package com.kameo.challenger.logic

import com.kameo.challenger.odb.*
import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.IRestrictions
import org.jinq.jpa.JPAJinqStream
import org.jinq.orm.stream.JinqStream
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject


@Component
public class TaskDAO {
    fun sum(t:TaskODB, e:Exception): Boolean { return t.getChallenge().id == 1L }

    @Inject
    lateinit var anyDao: AnyDAO;


    fun getTasks(callerId: Long, challengeId: Long, date: Date): MutableList<TaskODB> {
        //var k: KotlinEntityDataStore?;
/*        anyDao.getOne(ChallengeParticipantODB::class.java,IRestrictions { criteriaBuilder, criteriaQuery, root ->
           // root.get(

        });*/

 /*       var temp=anyDao.streamAll(TaskODB::class.java)
                .where<Exception,TaskODB> { t->t.getTaskStatus() == TaskStatus.accepted }.collect(Collectors.toList<TaskODB>());

        println(temp);*/


        val callerPermission = anyDao.getOnlyOne(ChallengeParticipantODB::class.java, { cp -> cp.getChallenge().id == challengeId
                && cp.getUser().id == callerId })
        if(callerPermission != null)
            updateSeendDateOfChallegeContract(callerPermission);



        val res=anyDao.streamAll(TaskODB::class.java)
                .where<Exception,TaskODB>({t->t.getChallenge().id == challengeId})
         .filter({t->t.taskType != TaskType.onetime ||  DateTime(t.getDueDate()).isAfter(date.getTime())}).collect(Collectors.toList<TaskODB>());



        getTaskProgress(res, date).forEach { tp -> res.filter({ t->t.id == tp.task.id}).find { tp.isDone } }
        return res
    }



    fun getTaskProgress(tasks: MutableList<TaskODB>, day: Date): List<TaskProgressODB> {
        val midnight = DateUtil.getMidnight(day)

        val taskIds = tasks.map({ t -> t.id });//.collect(Collectors.toList<Long>())
        if (taskIds.isEmpty())
            return emptyList()
        return anyDao?.streamAll(TaskProgressODB::class.java).where<Exception,TaskProgressODB>({ tp -> taskIds.contains(tp.getTask().id) && tp.getProgressTime() == midnight }).collect(Collectors.toList<TaskProgressODB>())
    }


    private fun updateSeendDateOfChallegeContract(cp: ChallengeParticipantODB) {
        cp.lastSeen = Date()
        anyDao?.getEm()?.merge(cp)
    }
}