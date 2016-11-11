package com.kameo.challenger.domain.reports

import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.reports.ReportDAO.ProgressiveReportDTO
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.domain.tasks.db.TaskProgressODB
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by Kamila on 2016-11-10.
 */
@Component
@Transactional
open class ReportDAO(@Inject val anyDaoNew: AnyDAONew, @Inject val permissionDAO: PermissionDAO)  {


    open fun getProgressiveReport(callerId: Long, challengeId: Long, dayFromMidnight: LocalDate): ProgressiveReportDTO {
        permissionDAO.checkHasPermissionToChallenge(callerId, challengeId);


        val challengeODB = anyDaoNew.find(ChallengeODB::class, challengeId);

        val allTasks = anyDaoNew.getAll(TaskProgressODB::class) {
            it get TaskProgressODB::task get TaskODB::challenge eqId challengeId
            it get TaskProgressODB::done eq true
            it.orderByAsc(TaskProgressODB::progressTime)
            //it.get(+TaskProgressODB::progressTime) greaterThanOrEqualTo dayFrom.atStartOfDay()
        }




        val report = allTasks.groupBy { it.task.user.id }.map {

            val userDayMap = mutableMapOf<LocalDate, Int>();
            it.value.sortedBy(TaskProgressODB::progressTime).forEach {
                val points = it.task.difficulty + 1

                if (it.progressTime < dayFromMidnight) {
                    val p = userDayMap.getOrElse(dayFromMidnight, { 0 });
                    userDayMap.put(dayFromMidnight, p + points)
                } else {
                    val date = it.progressTime;
                    val p = userDayMap.getOrElse(date, { 0 });
                    userDayMap.put(date, p + points)

                }
            }

            var lastVal: Int = 0;
            userDayMap.keys.sortedBy { it }.forEach {
                var newVal: Int = userDayMap.get(it)!!;
                lastVal += newVal;
                userDayMap.put(it, lastVal);
            }
            Pair(it.key, userDayMap)

        }.fold(ProgressiveReportDTO(dayFromMidnight)) { rep, next ->
            val userId=next.first
            rep.data.put(userId, next.second)
            rep.userLabels.put(userId,challengeODB.participants.find {p->p.user.id==userId}!! . user.getLoginOrEmail());
            rep
        };


        return report;


    }


    class ProgressiveReportDTO(val dayFromMidnight: LocalDate= LocalDate.now()) {

        val userLabels: MutableMap<Long,String> = mutableMapOf()
        val data: MutableMap<Long, Map<LocalDate, Int>> = mutableMapOf()
    }

}