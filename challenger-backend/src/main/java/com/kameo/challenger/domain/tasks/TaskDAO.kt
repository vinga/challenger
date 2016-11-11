package com.kameo.challenger.domain.tasks

import com.google.common.base.Strings
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.tasks.db.*
import com.kameo.challenger.domain.tasks.db.TaskStatus.accepted
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
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

    open fun getTasks(callerId: Long, challengeId: Long, dayMidnight: LocalDate): List<TaskODB> {

//        anyDaoNew.test();

        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class) {
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        }
        updateSendDateOfChallengeContract(callerPermission0)

        val tasks = anyDaoNew.getAll(TaskODB::class) {
            it get TaskODB::challenge eqId challengeId
            it.newOr {
                it get TaskODB::taskType notEq TaskType.onetime
                it get (+TaskODB::dueDate) after dayMidnight.atStartOfDay()
            }
        }
        tasks.forEach {
            println(""+it.dueDate+" "+dayMidnight+" "+dayMidnight.atStartOfDay())
            if (it.dueDate!=null) {
                println(""+Timestamp.valueOf(it.dueDate)+" "+Timestamp.valueOf(dayMidnight.atStartOfDay()))
            }
        }

        getTaskProgress(tasks, dayMidnight).forEach {
            tp ->
            tasks
                    .filter { it.id == tp.task.id }
                    .forEach { it.done = tp.done }
        }
        return tasks
    }


    open fun deleteTask(callerId: Long, taskId: Long): TaskODB {
        val task = anyDaoNew.find(TaskODB::class, taskId)
        if (task.user.id != callerId)
            throw IllegalArgumentException()

        anyDaoNew.remove(TaskProgressODB::class) { it get TaskProgressODB::task eqId taskId }
        anyDaoNew.remove(TaskApprovalODB::class) { it get TaskApprovalODB::task eqId taskId }
        return anyDaoNew.remove(task)
    }

    open fun getTaskProgress(tasks: List<TaskODB>, dayMidnight: LocalDate): List<TaskProgressODB> {

        val taskIds = tasks.map { it.id }
        return if (taskIds.isEmpty())
            emptyList()
        else
            anyDaoNew.getAll(TaskProgressODB::class, {
                it.get(TaskProgressODB::task).get(TaskODB::id) isIn taskIds
                it get TaskProgressODB::progressTime eq dayMidnight
            })

    }

    open fun markTaskDone(callerId: Long, challengeId: Long, taskId: Long, dayMidnight: LocalDate, done: Boolean): TaskProgressODB {
        permissionDao.checkHasPermissionToChallenge(callerId, challengeId)
        val task = anyDaoNew.getOne(TaskODB::class) { it eqId taskId }
        if (task.user.id != callerId)
            throw IllegalArgumentException()
        if (task.challenge.id!=challengeId)
            throw IllegalArgumentException()

        val taskProgress = anyDaoNew.getFirst(TaskProgressODB::class) {
            it.get(TaskProgressODB::task) eqId taskId
            it.get(TaskProgressODB::progressTime) eq dayMidnight
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




    open fun changeTaskStatus(challengeId: Long, taskId: Long, userIds: Set<Long>, taskStatus: TaskStatus=accepted, rejectionReason: String?): TaskODB {
        val existingApprovals = anyDaoNew.getAllMutable(TaskApprovalODB::class) {
            it get(TaskApprovalODB::task) eqId taskId
        }
        val task = anyDaoNew.find(TaskODB::class, taskId)
        val challenge = task.challenge


        if (userIds.isEmpty())
            throw IllegalArgumentException("Callers not specified");
        for (userId in userIds) {
            permissionDao.checkHasPermissionToTask(userId, taskId);
            permissionDao.checkHasPermissionToChallenge(userId, challengeId);
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

        challenge.participants.map { it.user }.forEach { u ->
            if (userIds.contains(u.id)) {
                var ta = existingApprovals.find { ta -> ta.user.id == u.id }
                if (ta!=null) {
                    ta.rejectionReason = rejectionReason
                    ta.taskStatus = taskStatus
                    anyDaoNew.merge(ta)
                } else {
                    ta = TaskApprovalODB(user = u, task = task, taskStatus = taskStatus)
                    ta.rejectionReason = rejectionReason
                    anyDaoNew.em.persist(ta)
                    existingApprovals.add(ta)
                }
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
}