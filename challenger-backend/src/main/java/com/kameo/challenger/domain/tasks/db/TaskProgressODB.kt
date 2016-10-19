package com.kameo.challenger.domain.tasks.db

import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.odb.api.IIdentity
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
data class TaskProgressODB(@Id
                           @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                           override val id: Long = 0) : IIdentity {



    lateinit var progressTime: Date

    var done: Boolean = false

    @NotNull
    @ManyToOne
    lateinit var task: TaskODB
}