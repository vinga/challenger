package com.kameo.challenger.domain.tasks

import com.google.common.base.Strings
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.tasks.db.*
import com.kameo.challenger.domain.tasks.db.TaskStatus.accepted
import com.kameo.challenger.domain.tasks.db.TaskType.*
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


@Transactional
@Component
open class TaskDAO {

    @Inject
    private lateinit var anyDaoNew: AnyDAONew
    @Inject
    private lateinit var permissionDao: PermissionDAO


    open fun createTask(callerId: Long, taskODB: TaskODB): TaskODB {
        val cc = anyDaoNew.find(ChallengeODB::class, taskODB.challenge.id)
        if (!cc.participants.any { it.user.id == callerId })
            throw IllegalArgumentException()
        if (!cc.participants.any { it.user.id == taskODB.user.id })
            throw IllegalArgumentException()
        if (taskODB.startDate?.isAfter(taskODB.dueDate) ?: false) {
            throw IllegalArgumentException("Start date cannot be bigger than due date")
        }

        normalizeWeekdays(taskODB)
        normalizeMonthdays(taskODB)



        taskODB.createdByUser = UserODB(callerId)
        taskODB.taskStatus =
                /*if (taskODB.user.id == callerId)
                    TaskStatus.accepted
                else*/
                TaskStatus.waiting_for_acceptance

        anyDaoNew.persist(taskODB)
        anyDaoNew.persist(TaskApprovalODB(taskODB.createdByUser, taskODB, TaskStatus.accepted))
        return taskODB
    }

    private fun normalizeWeekdays(taskODB: TaskODB) {
        val minDayNoIWeek = 1
        val maxDayNoIWeek = 7
        taskODB.weekDays?.let {
            if (isAllSelected(it, minDayNoIWeek, maxDayNoIWeek))
                taskODB.weekDays = null
        }
    }

    private fun normalizeMonthdays(taskODB: TaskODB) {
        val minDayNoInMonth = 0
        val maxDayNoInMonth = 30
        taskODB.monthDays?.let {
            if (isAllSelected(it, minDayNoInMonth, maxDayNoInMonth))
                taskODB.monthDays = null
        }
    }
    open fun getTasksById(callerId: Long, challengeId: Long, ids: List<Long>): List<TaskODB> {
        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class) {
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        }
        return anyDaoNew.getAll(TaskODB::class) {
            it get TaskODB::challenge eqId challengeId
            it inIds ids
        }
    }

    private fun isAllSelected(it: String, min: Int, max: Int): Boolean
            = it
            .split(",")
            .filter { it.length > 0 }
            .map(String::toInt)
            .containsAll((min..max).toList())


    open fun getTasks(callerId: Long, challengeId: Long, dayMidnight: LocalDate): List<TaskODB> {

        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class) {
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        }
        updateSendDateOfChallengeContract(callerPermission0)

        if (false) {
            val tasksTests = anyDaoNew.getAll(TaskODB::class) {
                it get TaskODB::challenge eqId challengeId

                val tit=it
               // val subq = it.subquery(UserODB::class);
               // val sit = subq.from(UserODB::class);
                it.get(TaskODB::label).isInSubquery(UserODB::class) {
                    it.get(+UserODB::login) like "kami"
                    it.select(+UserODB::login)
                }

                it.get(TaskODB::label) isIn it.subqueryFrom(UserODB::class) {
                    it.get(+UserODB::login) like "kami"
                    it.select(+UserODB::login)
                }
               it.get(TaskODB::label) exists it.subqueryFrom(UserODB::class) {
                    it.get(+UserODB::login) like "kami"
                    it.get(+UserODB::id) eq tit.get(TaskODB::id)
                }

              /*  it.get(TaskODB::createdByUser) isIn it.subqueryFrom(UserODB::class) {
                    it.get(+UserODB::login) like "kami"
                }*/
                        /*   it.get(TaskODB:label) like it.subqueryFrom(UserODB.class) {
                    it.get(UserODB::email) like 'kami'
                    it.select(UserODB::email)
                } */
                it
            }
        }

        val tasks = anyDaoNew.getAll(TaskODB::class) {
            it get TaskODB::challenge eqId challengeId
            //it get TaskODB::num min{} ge 10

            it get TaskODB::createDate before dayMidnight.plusDays(1).atStartOfDay()
            ors {
                it get TaskODB::closeDate isNull {}
                it get +TaskODB::closeDate afterOrEqual dayMidnight
            }

            // limit visibility for custom dates
            and {
                or {
                    it get TaskODB::taskType eq TaskType.onetime

                    it get +TaskODB::startDate before dayMidnight.plusDays(1).atStartOfDay()


                    // for onetimes only if 'not done' OR 'is done exactly at dayMidnight'
                    it get +TaskODB::dueDate after dayMidnight.minusDays(1).atStartOfDay()
                    ors {
                        it get TaskODB::closeDate isNull {}
                        it get TaskODB::closeDate eq dayMidnight
                    }
                }
                or {
                    it get TaskODB::taskType eq TaskType.monthly
                    it get +TaskODB::monthDays isNullOrContains ",${dayMidnight.dayOfMonth},"
                }
                or {
                    it get TaskODB::taskType eq TaskType.weekly
                    it get +TaskODB::weekDays isNullOrContains ",${dayMidnight.dayOfWeek.value},"
                }
                or {
                    it get TaskODB::taskType eq TaskType.daily
                    it get +TaskODB::monthDays isNullOrContains ",${dayMidnight.dayOfMonth},"
                    it get +TaskODB::weekDays isNullOrContains ",${dayMidnight.dayOfWeek.value},"
                }
            }
        }


        tasks.forEach {
            println("visible task: " + it.dueDate + " " + dayMidnight + " " + dayMidnight.atStartOfDay() + ", close date: " + it.closeDate + ", day midnight:" + dayMidnight)
            if (it.dueDate != null) {
                println("" + Timestamp.valueOf(it.dueDate) + " " + Timestamp.valueOf(dayMidnight.atStartOfDay()))
            }
        }

        getTaskProgress(tasks, dayMidnight).forEach {
            it.task.done = it.done
        }

        val tasksIdDoneInPeriod = getTaskProgressDoneInPeriod(tasks, dayMidnight)
        return tasks.filter {
            it.id !in tasksIdDoneInPeriod
        }
    }


    open fun deleteTask(callerId: Long, taskId: Long): TaskODB {
        val task = anyDaoNew.find(TaskODB::class, taskId)
        if (task.user.id != callerId)
            throw IllegalArgumentException("Only user that has assigned task to him can delete it")

        anyDaoNew.remove(TaskProgressODB::class) { it get TaskProgressODB::task eqId taskId }
        anyDaoNew.remove(TaskApprovalODB::class) { it get TaskApprovalODB::task eqId taskId }
        return anyDaoNew.remove(task)
    }

    open fun getTaskProgressDoneInPeriod(tasks: List<TaskODB>, toDate: LocalDate): List<Long> {
        val startOfMonth = toDate.minusDays(toDate.atStartOfDay().dayOfMonth.toLong())
        val startOfWeek = toDate.minusDays(toDate.atStartOfDay().dayOfWeek.value.toLong())
        val taskIds = tasks.filter {
            !it.done && (it.taskType == monthly || it.taskType == weekly)
        }.map { it.id }

        return if (taskIds.isEmpty())
            emptyList()
        else
            anyDaoNew.getAll(TaskProgressODB::class) {
                it get TaskProgressODB::task get TaskODB::id isIn taskIds
                it get TaskProgressODB::done eq true
                and {
                    or {
                        it get TaskProgressODB::task get TaskODB::taskType eq monthly
                        it get TaskProgressODB::progressTime greaterThanOrEqualTo startOfMonth
                        it get TaskProgressODB::progressTime lessThan toDate
                    }
                    or {
                        it get TaskProgressODB::task get TaskODB::taskType eq weekly
                        it get TaskProgressODB::progressTime greaterThanOrEqualTo startOfWeek
                        it get TaskProgressODB::progressTime lessThan toDate
                    }
                }
                it selectDistinct it.get(TaskProgressODB::task).get(TaskODB::id)
            }
    }

    open fun getTaskProgress(tasks: List<TaskODB>, dayMidnight: LocalDate): List<TaskProgressODB> {
        val taskIds = tasks.map { it.id }
        return if (taskIds.isEmpty())
            emptyList()
        else
            anyDaoNew.getAll(TaskProgressODB::class) {
                it get TaskProgressODB::task get TaskODB::id isIn taskIds
                it get TaskProgressODB::progressTime eq dayMidnight
            }

    }

    open fun markTaskDone(callerId: Long, challengeId: Long, taskId: Long, dayMidnight: LocalDate, done: Boolean): TaskProgressODB {
        permissionDao.checkHasPermissionToChallenge(callerId, challengeId)
        val task = anyDaoNew.getOne(TaskODB::class) { it eqId taskId }
        if (task.user.id != callerId)
            throw IllegalArgumentException()
        if (task.challenge.id != challengeId)
            throw IllegalArgumentException()


        if (task.taskType == onetime) {
            if (task.closeDate != null && done)
                throw IllegalArgumentException("Onetime task is already marked as done")
            task.closeDate = if (done) dayMidnight else null
            anyDaoNew.merge(task)
        }

        val taskProgress = anyDaoNew.getFirst(TaskProgressODB::class) {
            it get TaskProgressODB::task eqId taskId
            it get TaskProgressODB::progressTime eq dayMidnight
        }

        return if (taskProgress != null) {
            taskProgress.done = done
            anyDaoNew.update(TaskProgressODB::class) {
                it.set(TaskProgressODB::done, done)
                        .eqId(taskProgress.id)
            }
            taskProgress
        } else {
            val tp = TaskProgressODB(task, dayMidnight, done)
            return anyDaoNew.merge(tp)
        }
    }

    private fun updateSendDateOfChallengeContract(cp: ChallengeParticipantODB) {
        cp.lastSeen = Date()
        anyDaoNew.merge(cp)
    }


    open fun changeTaskStatus(challengeId: Long, taskId: Long, userIds: Set<Long>, taskStatus: TaskStatus = accepted, rejectionReason: String?): TaskODB {
        val existingApprovals = anyDaoNew.getAllMutable(TaskApprovalODB::class) {
            it get (TaskApprovalODB::task) eqId taskId
        }
        val task = anyDaoNew.find(TaskODB::class, taskId)
        val challenge = task.challenge


        if (userIds.isEmpty())
            throw IllegalArgumentException("Callers not specified")
        for (userId in userIds) {
            permissionDao.checkHasPermissionToTask(userId, taskId)
            permissionDao.checkHasPermissionToChallenge(userId, challengeId)
        }
        if (taskStatus == TaskStatus.waiting_for_acceptance)
            throw IllegalArgumentException()
        if (taskStatus == TaskStatus.rejected && Strings.isNullOrEmpty(rejectionReason))
            throw IllegalArgumentException()
        if (task.taskStatus != TaskStatus.waiting_for_acceptance)
            throw IllegalArgumentException()
        val userIdsAreChallengeParticipants = challenge.participants.map({ cp -> cp.user.id }).containsAll(userIds)
        if (!userIdsAreChallengeParticipants)
            throw IllegalArgumentException()

        challenge.participants.map { it.user }.filter { it.id in userIds }.forEach { u ->
            var ta = existingApprovals.find { it.user.id == u.id }
            if (ta != null) {
                ta.rejectionReason = rejectionReason
                ta.taskStatus = taskStatus
                anyDaoNew.merge(ta)
            } else {
                ta = TaskApprovalODB(user = u, task = task, taskStatus = taskStatus)
                ta.rejectionReason = rejectionReason
                anyDaoNew.persist(ta)
                existingApprovals.add(ta)
            }
        }
        if (taskStatus == TaskStatus.rejected) {
            task.taskStatus = TaskStatus.rejected
            anyDaoNew.merge(task)
        }
        val allParticipantsAccepted = existingApprovals.filter { it.taskStatus == TaskStatus.accepted }.count() == challenge.participants.size
        if (allParticipantsAccepted) {
            task.taskStatus = TaskStatus.accepted
            anyDaoNew.merge(task)
        }
        return task
    }


    /**
     * task must be from same challenge
     */
    open fun getTasksApprovalsOtherThanAccepted(tasks: List<TaskODB>): List<TaskApprovalODB> {

        val mergedTasks = tasks.map { anyDaoNew.merge(it) }
        if (mergedTasks.isEmpty())
            return emptyList();

        val participants = mergedTasks.first().challenge.participants;
        val approvals = anyDaoNew.getAll(TaskApprovalODB::class) {
            it get TaskApprovalODB::task inIds mergedTasks.map { it.id }
            it get TaskApprovalODB::task get TaskODB::taskStatus notEq TaskStatus.accepted
        }

        val pendingApprovals = approvals.groupBy({ it.task.id }, { it.user.id }).flatMap {
            val (taskId, approvedUserIds) = it;
            participants.filter { it.user.id !in approvedUserIds }.map {
                TaskApprovalODB(it.user, TaskODB(taskId), TaskStatus.waiting_for_acceptance)
            }
        }
        return approvals + pendingApprovals;
    }

    open fun closeTask(callerId: Long, taskId: Long):TaskODB {
        val t=anyDaoNew.find(TaskODB::class, taskId);
        t.closeDate=LocalDate.now()
        anyDaoNew.merge(t)
        return t
    }

}