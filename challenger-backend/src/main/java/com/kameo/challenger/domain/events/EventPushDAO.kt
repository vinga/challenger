package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus
import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO
import com.kameo.challenger.logic.PermissionDAO
import com.kameo.challenger.utils.odb.AnyDAONew
import com.kameo.challenger.utils.odb.newapi.unaryPlus
import com.kameo.challenger.utils.synchCopyThenClear
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.ws.rs.container.AsyncResponse


@Transactional
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
open class EventPushDAO {
    @Inject
    lateinit var eventGroupDAO: EventGroupDAO
    @Inject
    lateinit var permissionDAO: PermissionDAO
    @Inject
    lateinit var anyDaoNew: AnyDAONew


    private var listeningUsers = mutableMapOf<Long, MutableList<UserClient>>()

    /**
     * check if there is anything unread for caller for specified challenge
     * if yes, return that immediately
     * otherwise add asyncResponse to list
     */
    open fun listenToNewEvents(uniqueClientIdentifier: String, callerId: Long, asyncResponse: AsyncResponse, challengeId: Long, lastReadEventId: Long?) {
        permissionDAO.checkHasPermissionToChallenge(callerId, challengeId)


        val userClient = UserClient(callerId, lastReadEventId, asyncResponse)
        if (broadcastEventsToCustomClient(userClient))
            return;


        synchronized(listeningUsers, {
            val list = listeningUsers.getOrPut(callerId) {
                mutableListOf()
            }
            synchronized(list, { list.add(userClient) })

        })


        asyncResponse.setTimeout(500, TimeUnit.SECONDS)
        asyncResponse.setTimeoutHandler { it.cancel() }

    }


    open fun broadcastNewEvent(challengeId: Long) {
        // fetch everyhing unread for those challenge and subscribers

        val userIds = anyDaoNew.getAll(ChallengeParticipantODB::class) {
            it get ChallengeParticipantODB::challenge eqId challengeId
            it get ChallengeParticipantODB::challengeStatus notEq ChallengeStatus.REFUSED
            it.select(it get ChallengeParticipantODB::user get +UserODB::id)
        }


        userIds.forEach {
            listeningUsers[it]?.let {
                synchronized(it) {
                    it.forEach {
                        broadcastEventsToCustomClient(it)
                    }
                    it.clear()
                }
            }
        }


    }


    private fun broadcastEventsToCustomClient(client: UserClient): Boolean {
        val events = if (client.lastReadEventId == null) {
            // first time after login we don't have last read event id
            eventGroupDAO.getUnreadEventsForChallenge(client.userId, null)
        } else {
            eventGroupDAO.getLaterEventsForChallenge(client.userId, null, client.lastReadEventId)
        } .map { EventDTO.fromODB(it) }


        if (events.isEmpty())
            return false
        client.asyncResponse.resume(events.toTypedArray())

        return true
    }




    private class UserClient(
            val userId: Long,
            val lastReadEventId: Long?,
            val asyncResponse: AsyncResponse)

}
