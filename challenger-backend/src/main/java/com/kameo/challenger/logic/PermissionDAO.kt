package com.kameo.challenger.logic

import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.utils.odb.AnyDAONew
import org.springframework.stereotype.Component
import javax.inject.Inject


@Component
open class PermissionDAO(@Inject val anyDaoNew: AnyDAONew) {

    fun checkHasPermissionToTask(callerId: Long, taskId: Long) {
        val exists = anyDaoNew.exists(TaskODB::class, {
            it eqId taskId
            it.join(TaskODB::challenge).joinList(ChallengeODB::participants)
                    .eqId(ChallengeParticipantODB::user, callerId)
                    .get(ChallengeParticipantODB::challengeStatus) eq ChallengeStatus.ACTIVE
        })
        if (!exists)
            throw IllegalArgumentException("No permission")
    }

    fun checkHasPermissionToChallenge(callerId: Long, challengeId: Long) {
        val exists = anyDaoNew.exists(ChallengeParticipantODB::class, {
            it get ChallengeParticipantODB::user eqId callerId
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::challengeStatus eq ChallengeStatus.ACTIVE
        })
        if (!exists)
            throw IllegalArgumentException("No permission")
    }
}
