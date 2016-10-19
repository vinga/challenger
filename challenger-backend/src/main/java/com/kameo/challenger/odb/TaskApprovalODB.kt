package com.kameo.challenger.odb

import com.kameo.challenger.odb.api.IIdentity
import javax.persistence.*


@Entity
data class TaskApprovalODB(@Id
                           @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                           override var id: Long = 0) : IIdentity {

    @ManyToOne
    lateinit var task: TaskODB;

    @ManyToOne
    lateinit var user: UserODB;

    @Enumerated
    lateinit var taskStatus: TaskStatus;

    var rejectionReason: String? = null
}