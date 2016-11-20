package com.kameo.challenger.domain.reports

import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.domain.tasks.db.TaskProgressODB
import com.kameo.challenger.domain.tasks.db.TaskStatus
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.iterator
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


@Component
@Transactional
open class ReportDAO(@Inject val anyDaoNew: AnyDAONew, @Inject val permissionDAO: PermissionDAO) {


    open fun getProgressiveReport(callerId: Long,
                                  challengeId: Long,
                                  dayFromMidnight: LocalDate,
                                  progressive: Boolean,
                                  countFromStart: Boolean
    ): ProgressiveReportDTO {
        permissionDAO.checkHasPermissionToChallenge(callerId, challengeId);
        val challengeODB = anyDaoNew.find(ChallengeODB::class, challengeId);
        val allTasks = anyDaoNew.getAll(TaskProgressODB::class) {
            it get TaskProgressODB::task get TaskODB::challenge eqId challengeId
            it get TaskProgressODB::task get TaskODB::taskStatus eq TaskStatus.accepted
            it get TaskProgressODB::done eq true
            if (!countFromStart)
                it.get(+TaskProgressODB::progressTime) ge dayFromMidnight
            it
        }

        val report = ProgressiveReportDTO(dayFromMidnight)

        val userToTasks = allTasks.groupBy { it.task.user.id };
        // fill not existing users
        challengeODB.participants.map {
            val userId = it.user.id

            // group by & sum by time
            val userDayMapFromDatabase = (userToTasks[userId] ?: emptyList())
                    .groupBy({ it.progressTime }, { it.task.difficulty + 1 })
                    .map {
                        Pair(it.key, it.value.reduce(Int::plus))
                    }.toMap()

            // iterate through days, add not existing days
            val values = TreeMap<LocalDate, Int>()
            var startDate = (userDayMapFromDatabase.keys + dayFromMidnight).min()!!
            for (temp in startDate..LocalDate.now()) {
                val points = userDayMapFromDatabase[temp] ?: 0
                values[temp] = points
            }

            if (progressive) {
                var last = 0
                values.forEach {
                    last += it.value
                    values[it.key] = last
                }
            }
            report.userLabels[userId] = it.user.getLoginOrEmail()
            report.data[userId] = values
        }
        return report;
    }


    class ProgressiveReportDTO(val dayFromMidnight: LocalDate = LocalDate.now()) {

        val userLabels: MutableMap<Long, String> = mutableMapOf()
        val data: MutableMap<Long, SortedMap<LocalDate, Int>> = mutableMapOf()
    }

}

