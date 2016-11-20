package com.kameo.challenger.domain.tasks.db

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.odb.api.IIdentity
import javax.persistence.*


@Entity
data class TaskApprovalODB(@Id
                           @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                           override var id: Long = 0) : IIdentity {

    @ManyToOne
    lateinit var task: TaskODB

    @ManyToOne
    lateinit var user: UserODB

    @Enumerated
    lateinit var taskStatus: TaskStatus

    var rejectionReason: String? = null

    constructor(user: UserODB, task: TaskODB, taskStatus: TaskStatus) : this(0) {
        this.task=task
        this.user=user
        this.taskStatus=taskStatus
    }
}