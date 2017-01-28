package com.kameo.challenger.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
open class ServerConfig {

    @Value("\${serverUrl}")
    lateinit var serverUrl: String


    var isCrossDomain = true
    val maxEventsSize:Int=500


    fun getConfirmEmailInvitationPattern(uid: String): String {
        return "$serverUrl#action={hash}".replace("{hash}", uid)
    }

    fun getLoginLink(): String {
        return serverUrl
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
