package com.kameo.challenger.logic

import com.kameo.challenger.domain.challenges.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.ChallengeStatus
import com.kameo.challenger.odb.TaskODB
import com.kameo.challenger.odb.TaskProgressODB
import com.kameo.challenger.odb.TaskType
import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject


@Component
public class TaskDAO {


    @Inject
    lateinit var anyDao: AnyDAO;

    @Inject
    lateinit var anyDaoNew: AnyDAONew;

    fun getTasks(callerId: Long, challengeId: Long, date: Date): List<TaskODB> {


        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class.java,
                {
                    it get ChallengeParticipantODB::challenge eqId challengeId
                    it get ChallengeParticipantODB::user eqId callerId
                    it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
                });
        updateSeendDateOfChallegeContract(callerPermission0);


        val tasks = anyDaoNew.getAll(TaskODB::class,
                {
                    it.get(TaskODB::challenge) eqId challengeId
                    it.newOr()
                            .notEq(TaskODB::taskType, TaskType.onetime)
                            .after(TaskODB::dueDate, date)
                });


        getTaskProgress(tasks, date).forEach { tp ->
            tasks.filter({ t -> t.id == tp.task.id })
                    .forEach { it.done = (tp.done); }
        }

        return tasks;

    }


    fun getTaskProgress(tasks: List<TaskODB>, day: Date): List<TaskProgressODB> {
        val midnight = DateUtil.getMidnight(day)

        val taskIds = tasks.map({ t -> t.id });//.collect(Collectors.toList<Long>())
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