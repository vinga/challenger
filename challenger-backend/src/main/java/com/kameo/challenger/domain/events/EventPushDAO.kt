package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO
import com.kameo.challenger.logic.PermissionDAO
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
    private var subscribers = mutableMapOf<Long, ChallengeSubscribers>()


    /**
     * check if there is anything unread for caller for specified challenge
     * if yes, return that immediately
     * otherwise add asyncResponse to list
     */
    open fun listenToNewEvents(uniqueClientIdentifier: String, callerId: Long, asyncResponse: AsyncResponse, challengeId: Long, lastReadEventId: Long?) {
        permissionDAO.checkHasPermissionToChallenge(callerId, challengeId)

        val subscriber = Subscriber(uniqueClientIdentifier = uniqueClientIdentifier,
                userId = callerId,
                challengeId = challengeId,
                asyncResponse = asyncResponse,
                lastReadEventId = lastReadEventId)


        val challengeSubscribers: ChallengeSubscribers? = subscribers[challengeId]
        if (challengeSubscribers == null) {
            synchronized(subscribers, {
                val challengeSubscribersSynch = subscribers[challengeId]
                if (challengeSubscribersSynch == null) {
                    // create new ChallengeSubscribers
                    subscribers.put(challengeId, ChallengeSubscribers(users = mutableListOf(subscriber)))
                } else {
                    synchronized(challengeSubscribersSynch, {
                        challengeSubscribersSynch.users.add(subscriber)
                    })
                }
            })

        } else {
            synchronized(challengeSubscribers, {
                challengeSubscribers.users.add(subscriber)
            })
        }

        asyncResponse.setTimeout(500, TimeUnit.SECONDS)
        asyncResponse.setTimeoutHandler { it.cancel() }

        //broadcastAllUnreadEvents(callerId, challengeId);

        broadcastEventsToCustomClient(subscriber)

    }


    open fun broadcastNewEvent(challengeId: Long) {
        // fetch everyhing unread for those challenge and subscribers

        subscribers[challengeId]?.let {
            synchronized(it) {
                it.users.toList().forEach {
                    broadcastEventsToCustomClient(it)
                }
            }
        }
    }

    /**
     * check if there are any unread posts, if yes, send them immediatelly
     */
    private fun broadcastAllUnreadEvents(callerId: Long, challengeId: Long) {
        eventGroupDAO.getUnreadEventsForChallenge(callerId, challengeId)
                .map { EventDTO.fromODB(it) }
                .toTypedArray()
                .let { internalBroadcastNewEvent(*it) }
    }

    /**
     * check if there are any posts with greater ID, if yes, send them immediatelly
     */
    private fun broadcastEventsToCustomClient(subscriber: Subscriber) {
        if (subscriber.lastReadEventId==null) {
            // first time after login we don't have last read event id
            broadcastAllUnreadEvents(subscriber.userId, subscriber.challengeId)
            return

        }
        val events = eventGroupDAO.getLaterEventsForChallenge(subscriber.userId, subscriber.challengeId, subscriber.lastReadEventId)
                .map { EventDTO.fromODB(it) }

        if (events.isEmpty())
            return
        if (events.groupBy { it.challengeId }.keys.size > 1)
            throw IllegalArgumentException("Only events from same event may be broadcasted together")

        subscribers[subscriber.challengeId]?.let {
            synchronized(it) {
                val sub = it.users.find { it.uniqueClientIdentifier == subscriber.uniqueClientIdentifier }
                if (sub != null) {
                    it.users.remove(sub)
                    sub.asyncResponse.resume(events.toTypedArray())
                }
            }
        }

    }


    /**
     * eventDtoArray - should be from same challenge
     */
    private fun internalBroadcastNewEvent(vararg eventDtoArray: EventDTO) {
        println("BROADCAST NEW EVENTS: " + eventDtoArray.size)
        if (eventDtoArray.isEmpty()) {
            return
        }
        if (eventDtoArray.groupBy { it.challengeId }.keys.size > 1)
            throw IllegalArgumentException("Only events from same challenge can be broadcast together")



        subscribers[eventDtoArray.first().challengeId]?.let {
            synchronized(it) {
                it.users.synchCopyThenClear().forEach {
                    it.asyncResponse.resume(eventDtoArray)
                }
            }
        }
    }


    private class ChallengeSubscribers(val users: MutableList<Subscriber> = mutableListOf())

    private class Subscriber(val uniqueClientIdentifier: String,
                             val userId: Long,
                             val challengeId: Long,
                             val asyncResponse: AsyncResponse, val lastReadEventId: Long?)


}
