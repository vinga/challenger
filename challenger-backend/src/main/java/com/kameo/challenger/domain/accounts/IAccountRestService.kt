package com.kameo.challenger.domain.accounts

import com.kameo.challenger.domain.accounts.db.UserRegistrationType


interface IAccountRestService {

    fun registerUser(login:String, email: String, userRegistrationType: UserRegistrationType);

}