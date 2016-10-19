package com.kameo.challenger.domain.challenges

import com.kameo.challenger.domain.challenges.ChallengeODB
import com.kameo.challenger.domain.challenges.ChallengeStatus
import com.kameo.challenger.odb.UserODB
import com.kameo.challenger.odb.api.IIdentity
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
data class ChallengeParticipantODB( @Id
                    @GeneratedValue(strategy= javax.persistence.GenerationType.AUTO)
                    override var id:Long=0) : IIdentity {


    @ManyToOne
    lateinit var challenge: ChallengeODB

    @ManyToOne
    lateinit var user: UserODB

    @Temporal(TemporalType.TIMESTAMP)
    var lastSeen: Date?=null;


    @NotNull
    @Enumerated
    var challengeStatus: ChallengeStatus = ChallengeStatus.WAITING_FOR_ACCEPTANCE;
}