package com.kameo.challenger.domain.tasks.db

import com.kameo.challenger.odb.api.IIdentity
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
data class TaskProgressODB(@Id
                           @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                           override val id: Long = 0) : IIdentity {



    lateinit var progressTime: LocalDate

    var done: Boolean = false

    @NotNull
    @ManyToOne
    lateinit var task: TaskODB

    constructor(task: TaskODB, progressTime: LocalDate, done: Boolean) : this(0) {
        this.task = task
        this.progressTime = progressTime
        this.done = done
    }
}