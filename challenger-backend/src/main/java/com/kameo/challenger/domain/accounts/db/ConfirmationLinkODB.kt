package com.kameo.challenger.domain.accounts.db

import com.kameo.challenger.odb.api.IIdentity
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*


@Entity
data class ConfirmationLinkODB(@Id
                               @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                               override var id: Long = 0) : IIdentity {


    lateinit var uid: String

    @Enumerated
    lateinit var confirmationLinkType: ConfirmationLinkType

    var fieldLogin: String? = null
    var fieldPasswordHash: String? = null
    var fieldSalt: String? = null
    var challengeId: Long? = null

    val sysCreationDate:LocalDateTime= LocalDateTime.now()
    @ManyToOne
    lateinit var user: UserODB;
}