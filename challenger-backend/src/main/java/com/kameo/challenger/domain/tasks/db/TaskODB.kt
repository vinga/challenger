package com.kameo.challenger.domain.tasks.db

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.odb.api.IIdentity
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*


@Entity
data class TaskODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   override var id: Long = 0) : IIdentity {


// var num: Int=10

    @Enumerated
    lateinit var taskType: TaskType

    var difficulty: Int = 0

    lateinit var label: String

    @Enumerated
    lateinit var taskStatus: TaskStatus


    @ManyToOne
    lateinit var user: UserODB

    @ManyToOne
    lateinit var createdByUser: UserODB


    var createDate: LocalDateTime = LocalDateTime.now()

    @ManyToOne
    lateinit var challenge: ChallengeODB


    var icon: String? = null


    // from when task will be visible
    var startDate: LocalDateTime? = null
    var dueDate: LocalDateTime? = null



    var closeDate: LocalDate? = null

    @Transient
    var done: Boolean = false

    var monthDays: String? = null
    var weekDays: String? = null



    enum class TaskDifficulty {
        EASY,
        MEDIUM,
        HARD
    }

}
