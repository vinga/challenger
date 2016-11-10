package com.kameo.challenger.domain.events.db

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.db.EventType
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.odb.api.IIdentity
import org.crsh.cli.Man
import java.util.*
import javax.persistence.*
import javax.persistence.TemporalType.TIMESTAMP

/**
 * Created by Kamila on 2016-10-21.
 */
@Entity
data class EventODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   override val id: Long = 0) : IIdentity {

    @ManyToOne
    @JoinColumn(updatable = false)
    var task: TaskODB? = null


    @ManyToOne
    @JoinColumn(updatable = false)
    lateinit var author: UserODB;

    @ManyToOne
    @JoinColumn(updatable = false)
    var recipient: UserODB? = null

    @JoinColumn(updatable = false)
    @Temporal(TIMESTAMP)
    var createDate: Date = Date();
    lateinit var content: String;

    @ManyToOne
    @JoinColumn(updatable = false)
    lateinit var challenge: ChallengeODB;

    @Enumerated
    lateinit var eventType: EventType;

}