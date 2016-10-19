package com.kameo.challenger.domain.tasks.db

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.tasks.db.TaskStatus
import com.kameo.challenger.domain.tasks.db.TaskType
import com.kameo.challenger.odb.api.IIdentity
import java.util.*
import javax.persistence.*

@Entity
data class TaskODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   override var id: Long = 0) : IIdentity {


    @Enumerated
    lateinit var taskType: TaskType;

    var difficulty: Int = 0;

    lateinit var label: String;

    @Enumerated
    lateinit var taskStatus: TaskStatus


    @ManyToOne
    lateinit var user: UserODB;

    @ManyToOne
    lateinit var createdByUser: UserODB;

    @ManyToOne
    lateinit var challenge: ChallengeODB;


    var icon: String? = null

    @Temporal(TemporalType.TIMESTAMP)
    var dueDate: Date? = null

    @Transient
    var done: Boolean = false

}