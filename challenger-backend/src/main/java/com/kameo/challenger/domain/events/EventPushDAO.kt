package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.accounts.EventGroupDAO
import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO
import com.kameo.challenger.domain.events.IEventGroupRestService.EventGroupDTO
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
    private var subscribers = mutableMapOf<Long, ChallengeSubscribers>();


    open fun listenToNewEvents(callerId: Long, asyncResponse: AsyncResponse, challengeId: Long) {
        //TODO permissions

        //TODO inform when more should be fetched??
        //TODO timeouts
        //TODO synchronization
        //TODO fetch only last messages
        // TODO what if in the meantime between remove response and next call somethin new arrived? better than remove, we should set there LAST postId


        val subscriber = Subscriber(callerId, asyncResponse)

        val challengeSubscribers: ChallengeSubscribers? = subscribers[challengeId]
        if (challengeSubscribers == null) {

            synchronized(subscribers, {
                val challengeSubscribersSynch: ChallengeSubscribers? = subscribers[challengeId];

                if (challengeSubscribersSynch == null) {
                    subscribers.put(challengeId, ChallengeSubscribers(users = mutableListOf(subscriber)))
                } else {

                    synchronized(challengeSubscribersSynch, {
                        challengeSubscribersSynch.users.add(subscriber)
                    });
                }

            });

        } else {
            synchronized(challengeSubscribers, {
                challengeSubscribers.users.add(subscriber);
            });
        }

        asyncResponse.setTimeout(500, TimeUnit.SECONDS);
        asyncResponse.setTimeoutHandler({


            it.cancel();


        })
    }


    open fun broadcastNewEvent(event: EventODB) {
        println("BROADCAST NEW");

        val challengeId = event.challenge.id;
        val postsForTask = eventGroupDAO.getPostsForChallengeWithoutPermissionCheck(challengeId).map(
                { EventDTO.fromODB(it) })
                .toTypedArray();


        val challengeSubscribers: ChallengeSubscribers? = subscribers.get(challengeId);
        if (challengeSubscribers != null) {
            synchronized(challengeSubscribers, {
                challengeSubscribers.users.forEach {

                    it.asyncResponse.resume(EventGroupDTO(challengeId, null, postsForTask))
                    it.lastDispatchedSentId = event.id
                }
                challengeSubscribers.users.clear()
            });
        }
        println("RESUME " + challengeSubscribers?.users?.size)


    }


    var lastDispatchedEventId: Number? = null;


    // TRANSIENT
    var minEventId: Long? = null;
    var maxEventId: Long? = null;

    open fun checkIfSomethingWasDispatchedAfter(minEventIdExclusive: Long?, maxEventIdExclusive: Long?) {
        val minEventIdExclusiveConst = minEventIdExclusive;
        val maxEventIdExclusiveConst = maxEventIdExclusive;
        if (minEventIdExclusiveConst == null) {
            // this should only have place after system start, for example when first person registers to listen
            minEventId = eventGroupDAO.getMaxEventId();
            maxEventId = eventGroupDAO.getMaxEventId();
        }
        if (minEventIdExclusiveConst != null && maxEventIdExclusiveConst != null && minEventIdExclusiveConst + 1 == maxEventIdExclusiveConst) {
            // nothing more for sure
            minEventId = minEventId;
            maxEventId = maxEventIdExclusive;
        } else {
            var notDispatchedEvents = eventGroupDAO.getEventsBetween(minEventIdExclusive, maxEventIdExclusive);
            notDispatchedEvents.forEach {
                broadcastNewEvent(it)
            }
            minEventId = notDispatchedEvents.maxBy({ it.id })?.id;
            maxEventId = null;
        }
    }


    private class ChallengeSubscribers(val users: MutableList<Subscriber> = mutableListOf()) {

    }

    private class Subscriber(val userId: Long, val asyncResponse: AsyncResponse, var lastDispatchedSentId: Long? = null) {

    }


}
