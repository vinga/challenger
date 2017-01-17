package com.kameo.challenger.domain.accounts

import com.kameo.challenger.utils.DateUtil
import com.kameo.challenger.utils.auth.jwt.JWTService
import com.kameo.challenger.utils.auth.jwt.JWTService.AuthException
import com.kameo.challenger.web.rest.ChallengerSess
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

/**
 * Created by Kamila on 2017-01-14.
 */
@Component
open class JWTokenDAO(@Inject private val jwtService: JWTService<ChallengerSess>) {


    @Throws(AuthException::class)
    fun createNewTokenFromUserId(userId: Long): String {
        val td = ChallengerSess()
        td.userId = userId
        td.expires = DateTime(DateUtil.addMinutes(Date(), 15))
        return jwtService.tokenToString(td)
    }


}