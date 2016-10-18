package com.kameo.challenger.logic

import com.kameo.challenger.odb.*
import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.IRestrictions
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject


@Component
public class TaskDAO {
    fun sum(t: TaskODB, e: Exception): Boolean {
        return t.getChallenge().id == 1L
    }

    @Inject
    lateinit var anyDao: AnyDAO;

    @Inject
    lateinit var anyDaoNew: AnyDAONew;

    fun getTasks(callerId: Long, challengeId: Long, date: Date): List<TaskODB> {


        /*val callerPermission0 = anyDao.getOne(ChallengeParticipantODB::class.java,
                { cb, criteriaQuery, root ->
                    cb.equal(root.get(ChallengeParticipantODB_.challenge).get(ChallengeODB_.id), challengeId);
                });*/

        val callerPermission0 = anyDaoNew.getOne(ChallengeParticipantODB::class.java,
                { it.get(ChallengeParticipantODB_.challenge) eqId challengeId });
        if (callerPermission0 != null)
            updateSeendDateOfChallegeContract(callerPermission0);


        val tasks = anyDaoNew.getAll(TaskODB::class.java,
                {
                    it.get(TaskODB_.challenge) eqId challengeId
                    it.newOr()
                            .notEq(TaskODB_.taskType, TaskType.onetime)
                            .after(TaskODB_.dueDate, date)
                    .finish();

                });

        /*  val tasks = anyDao.get(TaskODB::class.java, { cb, criteriaQuery, root ->

              cb.equal(root.get(TaskODB_.challenge).get(ChallengeODB_.id), challengeId);

          }).filter { t -> t.taskType != TaskType.onetime || DateTime(t.getDueDate()).isAfter(date.getTime()) }*/

        getTaskProgress(tasks, date).forEach { tp -> tasks.filter({ t -> t.id == tp.task.id }).find { tp.isDone } }

        return tasks;
        /*  var test = { t: TaskODB -> t.getTaskStatus() == TaskStatus.accepted };
          com.user00.thunk.SerializedLambda.extractLambda(test);


          println(test); //Function1<com.kameo.challenger.odb.TaskODB, java.lang.Boolean>
          //   var temp=anyDao.streamAll(TaskODB::class.java)
          //           .where<Exception,TaskODB> { t->t.getTaskStatus() == TaskStatus.accepted }.collect(Collectors.toList<TaskODB>());

          //  println(temp);

  //sr >com.kameo.challenger.logic.TaskDAO$getTasks$callerPermission$1) 	$callerIdJ $challengeIdxp
          val callerPermission = anyDao.getOnlyOne(ChallengeParticipantODB::class.java, { cp ->
              cp.getChallenge().id == challengeId
                      && cp.getUser().id == callerId
          })
          if (callerPermission != null)
              updateSeendDateOfChallegeContract(callerPermission);


          val res = anyDao.streamAll(TaskODB::class.java)
                  .where<Exception, TaskODB>({ t -> t.getChallenge().id == challengeId })
                  .filter({ t -> t.taskType != TaskType.onetime || DateTime(t.getDueDate()).isAfter(date.getTime()) }).collect(Collectors.toList<TaskODB>());



          getTaskProgress(res, date).forEach { tp -> res.filter({ t -> t.id == tp.task.id }).find { tp.isDone } }
          return res*/
    }


    fun getTaskProgress(tasks: List<TaskODB>, day: Date): List<TaskProgressODB> {
        val midnight = DateUtil.getMidnight(day)

        val taskIds = tasks.map({ t -> t.id });//.collect(Collectors.toList<Long>())
        if (taskIds.isEmpty())
            return emptyList()

        return anyDao.get(TaskProgressODB::class.java, IRestrictions { cb, criteriaQuery, root ->

            cb.and(root.get(TaskProgressODB_.task).get(TaskODB_.id).`in`(taskIds),
                    cb.equal(root.get(TaskProgressODB_.progressTime), midnight))

        });

        //return anyDao?.streamAll(TaskProgressODB::class.java).where<Exception, TaskProgressODB>({ tp -> taskIds.contains(tp.getTask().id) && tp.getProgressTime() == midnight }).collect(Collectors.toList<TaskProgressODB>())
    }


    private fun updateSeendDateOfChallegeContract(cp: ChallengeParticipantODB) {
        cp.lastSeen = Date()
        anyDao?.getEm()?.merge(cp)
    }
}