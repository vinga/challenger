package com.kameo.challenger.domain.challenges

import com.google.common.collect.Lists
import com.kameo.challenger.domain.challenges.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.ChallengeStatus
import com.kameo.challenger.odb.UserODB
import com.kameo.challenger.odb.api.IIdentity
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
data class ChallengeODB(@Id
                        @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                        override var id: Long = 0) : IIdentity {

    var label: String? = null

    @ManyToOne
    lateinit var createdBy: UserODB;

    @Enumerated
    lateinit var challengeStatus: ChallengeStatus;

    @OneToMany(mappedBy = "challenge")
    var participants: List<ChallengeParticipantODB> = Lists.newArrayList<ChallengeParticipantODB>()
}