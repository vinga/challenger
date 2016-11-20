package com.kameo.challenger.domain.events.db

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.odb.api.IIdentity
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.persistence.TemporalType.TIMESTAMP


@Entity
data class EventODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   override val id: Long = 0) : IIdentity {


    @Column(updatable = false)
    var taskId: Long? = null


    @ManyToOne
    @JoinColumn(updatable = false)
    lateinit var author: UserODB

    @ManyToOne
    @JoinColumn(updatable = false)
    var recipient: UserODB? = null

    @JoinColumn(updatable = false)
    @Temporal(TIMESTAMP)
    var createDate: Date = Date()
    lateinit var content: String


    @JoinColumn(updatable = false)
    var forDay: LocalDate = LocalDate.now()

    @ManyToOne
    @JoinColumn(updatable = false)
    lateinit var challenge: ChallengeODB

    @Enumerated
    lateinit var eventType: EventType

}