package com.kameo.challenger.odb

import com.kameo.challenger.odb.api.IIdentity
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull


@Entity
data class ConfirmationLinkODB(@Id
                               @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                               override var id: Long = 0):IIdentity {


    lateinit var uid: String;

    @Enumerated
    lateinit var confirmationLinkType: ConfirmationLinkType;

    var fieldLogin: String? = null
    var fieldPasswordHash: String? = null
    var fieldSalt: String? = null
    var email: String? = null
    var challengeId: Long? = null
}