package com.kameo.challenger.config

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
open class ServerConfig {
    private val confirmEmailInvitationPattern = "http://blablah/uapi/confirm/{hash}"
    var isCrossDomain = true
    val maxEventsSize:Int=100;

    fun getConfirmEmailInvitationPattern(uid: String): String {
        return confirmEmailInvitationPattern.replace("{hash}", uid)
    }

    companion object {

        /**
         * Web services base path
         */
        @JvmField val vsPATH="/api"

        /**
         * Web services subPath
         */
        const val restPath=""
    }

}
