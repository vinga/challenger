package com.kameo.challenger.domain.accounts.db


import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.odb.api.IIdentity
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany


@Entity
data class UserODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   override val id: Long = 0) : IIdentity {


    var login: String? = null
    lateinit var email: String
    lateinit var salt: String
    lateinit var passwordHash: String
    lateinit var userStatus: UserStatus
    lateinit var suspendedDueDate: Date
    var userRegistrationType = UserRegistrationType.NORMAL
    var failedLoginsNumber: Int = 0

    fun getLoginOrEmail(): String {
        return login ?: email;

    }

    companion object {
        @JvmStatic fun ofEmail(email: String): UserODB {
            val u: UserODB = UserODB(-1);
            u.email = email;
            return u;
        }
    }

}


