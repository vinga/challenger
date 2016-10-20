package com.kameo.challenger.domain.challenges

import com.google.common.collect.Lists
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import lombok.Data


interface IChallengeRestService {

    fun getVisibleChallenges(): IChallengeRestService.VisibleChallengesDTO

    @Data
    class UserLabelDTO(val id: Long,
                       val label: String,
                       val login: String?) {

    }

    @Data
    data class VisibleChallengesDTO(val selectedChallengeId: Long) {
        val visibleChallenges: MutableList<ChallengeDTO> = Lists.newArrayList<ChallengeDTO>()

        @Data class ChallengeDTO(val id: Long,
                                 val label: String?,
                                 val challengeStatus: String,
                                 val creatorId: Long,
                                 val userLabels: Array<UserLabelDTO>) {


            var myId: Long = 0

            fun setCallerId(callerId: Long) {
                myId=callerId
                userLabels.sortBy { if (it.id==callerId) "A" else "Z"+it.label }
            }
            companion object {
                fun fromODB(c: ChallengeODB): ChallengeDTO {
                    val co = ChallengeDTO(
                            id = c.id,
                            label = c.label,
                            challengeStatus = c.challengeStatus.name,
                            creatorId = c.createdBy.id,
                            userLabels = c.participants.map({ cp -> cp.user })
                                    .map({ u -> UserLabelDTO(u.id, u.getLoginOrEmail(), u.login) })
                                    .toTypedArray())
                    return co
                }
            }
        }


    }
}