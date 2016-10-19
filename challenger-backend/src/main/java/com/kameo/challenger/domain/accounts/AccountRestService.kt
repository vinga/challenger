package com.kameo.challenger.domain.accounts

import com.kameo.challenger.domain.accounts.db.UserRegistrationType
import javax.inject.Inject


class AccountRestService : IAccountRestService {

    @Inject
    private lateinit var accountDao:AccountDAO

    override fun registerUser(login: String, email: String, userRegistrationType: UserRegistrationType) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}