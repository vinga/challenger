package com.kameo.challenger.domain.challenges

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.kameo.challenger.odb.UserODB
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.EntityHelper
import lombok.Getter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.inject.Inject


@Component
@Transactional
internal open class ChallengeDAO (@Inject val anyDaoNew:AnyDAONew){


    internal class ChallengeInfoDTO {
        @Getter
        var defaultChallengeId: Long? = null
            internal set
        @Getter
        var visibleChallenges: List<ChallengeODB> = Lists.newArrayList<ChallengeODB>()
            internal set
    }

    open fun getVisibleChallenges(callerId: Long): ChallengeInfoDTO {
        val res = ChallengeInfoDTO()

       var challengeParticipantsForThisUser = anyDaoNew.getAll(ChallengeParticipantODB::class,{

            it.get(ChallengeParticipantODB::user) eqId callerId

            it.get(ChallengeParticipantODB::challenge)
                .newOr()
                   .eq(ChallengeODB::challengeStatus, ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                   .eq(ChallengeODB::challengeStatus, ChallengeStatus.ACTIVE)
                   .newAnd()
                        .eq(ChallengeODB::challengeStatus, ChallengeStatus.REFUSED)
                        .get(ChallengeODB::createdBy) eqId callerId


        });

        res.visibleChallenges=challengeParticipantsForThisUser.map{it.challenge}.distinct();
        res.visibleChallenges.map { EntityHelper.initializeCollection(it.participants)  }



        res.defaultChallengeId=calculateLastSeenChallengeId(challengeParticipantsForThisUser, res.visibleChallenges)
        return res
    }

    private fun calculateLastSeenChallengeId(challengeParticipantsForThisUser: List<ChallengeParticipantODB>,
                                             visibleChallenges: List<ChallengeODB> ):Long {
        if (!visibleChallenges.isEmpty()) {
            var challengeToLastSeen = mutableMapOf<ChallengeODB, Date>();
            for (cp in challengeParticipantsForThisUser) {
                challengeToLastSeen.put(cp.challenge, cp.lastSeen ?: Date(0));
            }
            return visibleChallenges.sortedByDescending {
                challengeToLastSeen.get(it)
            }.first().id


        }
        throw IllegalArgumentException();
    }
}