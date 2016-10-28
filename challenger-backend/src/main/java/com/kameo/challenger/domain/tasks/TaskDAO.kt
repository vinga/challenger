package com.kameo.challenger.domain.tasks

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.tasks.db.*
import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.inject.Inject


@Transactional
@Component
open class TaskDAO {


    @Inject
    lateinit var anyDao: AnyDAO

    @Inject
    lateinit var anyDaoNew: AnyDAONew


    open fun createTask(callerId: Long, taskODB: TaskODB): TaskODB {
        val cc = anyDao.get(ChallengeODB::class.java, taskODB.challenge.id)
        if (!cc.participants.any({ cp -> cp.user.id == callerId }))
            throw IllegalArgumentException()
        if (!cc.participants.any({ cp -> cp.user.id == taskODB.user.id }))
            throw IllegalArgumentException()


        taskODB.taskStatus = TaskStatus.waiting_for_acceptance
        if (taskODB.user.id == callerId)
            taskODB.taskStatus = TaskStatus.accepted


        taskODB.createdByUser = UserODB(callerId)
        anyDao.getEm().persist(taskODB)


        val ta = TaskApprovalODB()
        ta.user = taskODB.createdByUser
        ta.task = taskODB
        ta.taskStatus = TaskStatus.accepted
        anyDao.getEm().merge(ta)

        return taskODB
    }

    open fun getTasks(callerId: Long, challengeId: Long, date: Date): List<TaskODB> {



        anyDaoNew.test();

        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class,
                {
                    it get ChallengeParticipantODB::challenge eqId challengeId
                    it get ChallengeParticipantODB::user eqId callerId
                    it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
                })
        updateSeendDateOfChallegeContract(callerPermission0)



        val tasks = anyDaoNew.getAll(TaskODB::class,
                {

                    it.get(TaskODB::challenge) eqId challengeId
                    it.newOr({
                        it get TaskODB::taskType notEq TaskType.onetime
                        it.after(TaskODB::dueDate, date)
                    })


                });



        getTaskProgress(tasks, date).forEach {
            tp ->
                tasks
                    .filter({ it.id == tp.task.id })
                    .forEach { it.done = tp.done }
        }

        return tasks

    }

    open fun deleteTask(callerId: Long, taskId: Long) {
        val task = anyDaoNew.getOne(TaskODB::class, { it eqId taskId });
        if (task.user.id != callerId)
            throw IllegalArgumentException()

        anyDaoNew.remove(TaskProgressODB::class, { it get TaskProgressODB::task eqId taskId });
        anyDaoNew.remove(TaskApprovalODB::class, { it get TaskApprovalODB::task eqId taskId });

        anyDao.getEm().remove(task)

    }

    open fun getTaskProgress(tasks: List<TaskODB>, day: Date): List<TaskProgressODB> {
        val midnight = DateUtil.getMidnight(day)

        val taskIds = tasks.map({ it.id })
        if (taskIds.isEmpty())
            return emptyList()

        return anyDaoNew.getAll(TaskProgressODB::class, {
            it.get(TaskProgressODB::task).get(TaskODB::id).isIn(taskIds)
            it.eq(TaskProgressODB::progressTime, midnight)
        })

    }


    private fun updateSeendDateOfChallegeContract(cp: ChallengeParticipantODB) {
        cp.lastSeen = Date()
        anyDao.getEm().merge(cp)
    }
}