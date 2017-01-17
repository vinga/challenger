package com.kameo.challenger.domain.challenges

import com.google.common.collect.Lists
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.IChallengeRestService.VisibleChallengesDTO.ChallengeDTO
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.challenges.db.ChallengeStatus.REMOVED
import lombok.Data
import java.time.LocalDateTime


interface IChallengeRestService {
    fun getVisibleChallenges(): IChallengeRestService.VisibleChallengesDTO
    fun createChallenge(challengeDTO: ChallengeDTO): ChallengeDTO
    fun acceptChallenge(challengeId: Long, accepted: Boolean)
    fun updateChallenge(challengeId: Long, challengeDTO: ChallengeDTO) : ChallengeDTO
    fun deleteChallenge(challengeId: Long) : Boolean

    @Data
    class UserLabelDTO(val id: Long=0,
                       val label: String="",
                       val login: String?=null,
                       val challengeStatus: String=ChallengeStatus.WAITING_FOR_ACCEPTANCE.name
    )

    @Data
    data class VisibleChallengesDTO(val selectedChallengeId: Long?) {
        val visibleChallenges: MutableList<ChallengeDTO> = Lists.newArrayList<ChallengeDTO>()

        @Data class ChallengeDTO(val id: Long=0,
                                 val label: String?=null,
                                 val challengeStatus: String=ChallengeStatus.WAITING_FOR_ACCEPTANCE.name,
                                 val creatorId: Long=0,
                                 val userLabels: Array<UserLabelDTO> = arrayOf()) {


            var myId: Long = 0

            fun setCallerId(callerId: Long) {
                myId = callerId
                userLabels.sortBy { if (it.id == callerId) "A" else "Z" + it.label }
            }

            companion object {
                @JvmStatic
                fun fromODB(c: ChallengeODB): ChallengeDTO {
                    val co = ChallengeDTO(
                            id = c.id,
                            label = c.label,
                            challengeStatus = c.challengeStatus.name,
                            creatorId = c.createdBy.id,
                            userLabels = c.participants
                                    .filter{it.challengeStatus!=REMOVED}
                                    .map({ cp -> UserLabelDTO(cp.user.id, cp.user.getLoginOrEmail(), cp.user.login, cp.challengeStatus.name) })
                                    .toTypedArray())
                    return co
                }
                @JvmStatic
                fun toODB(c: ChallengeDTO, existingUsers: List<UserODB>): ChallengeODB {
                    val odb = ChallengeODB()
                    if (c.id > 0)
                        odb.id = c.id
                    odb.challengeStatus = ChallengeStatus.valueOf(c.challengeStatus)
                    odb.createDate = LocalDateTime.now()
                    odb.createdBy = UserODB(c.creatorId)
                    odb.label = c.label
                    odb.participants = c.userLabels.map {
                        participant ->
                        val cp = ChallengeParticipantODB(0)
                        cp.challenge = odb
                        cp.user = existingUsers.find { it.getLoginOrEmail() == participant.label }
                                ?:
                                let { val u = UserODB(); u.email = participant.label; u }

                        cp
                    }
                    return odb
                }
            }
        }


    }
}