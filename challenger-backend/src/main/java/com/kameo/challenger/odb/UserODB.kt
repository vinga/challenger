package com.kameo.challenger.odb

import com.kameo.challenger.odb.api.IIdentity
import java.util.*
import javax.persistence.*


@Entity
data class UserODB( @Id
                    @GeneratedValue(strategy= javax.persistence.GenerationType.AUTO)
                    override val id:Long=0) : IIdentity {


    var login:String?=null;
    lateinit var email:String;
    lateinit var salt:String;
    lateinit var passwordHash:String;
    lateinit var userStatus:UserStatus;
    lateinit var suspendedDueDate:Date;
    var failedLoginsNumber:Int=0;
    var defaultChallengeContract:Long?=null;
    @ManyToMany(targetEntity = ChallengeODB::class)
    lateinit var contracts:MutableList<ChallengeODB>;

    fun getLoginOrEmail(): String {
        return login  ?:  email;

    }

     companion object {
         @JvmStatic fun ofEmail(email:String):UserODB
        {
            val u:UserODB = UserODB(-1);
            u.email=email;
            return u;
        }
    }

}


