package com.kameo.challenger.domain.events

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.EventGroupDAO
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO
import com.kameo.challenger.domain.events.IEventGroupRestService.EventGroupDTO
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus.CREATED
import com.kameo.challenger.web.rest.ChallengerSess
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType

@Component
@Path(ServerConfig.restPath)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api
class EventGroupRestService : IEventGroupRestService {

    @Inject
    private lateinit var eventGroupDAO: EventGroupDAO
    @Inject
    private lateinit var session: ChallengerSess;


    @GET
    @Path("/challenges/{challengeId}/tasks/{taskId}/events")
    @ApiOperation("Gets events for task")
    override fun getEventsForTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long): EventGroupDTO {
        val callerId = session.getUserId();

        val postsForTask = eventGroupDAO.getEventsForTask(callerId, challengeId, taskId).map(
                { EventDTO.fromODB(it) })
                .toTypedArray();

        return EventGroupDTO(challengeId, taskId, postsForTask);
    }


    private class ChallengeSubscribers(val users: MutableList<Subscriber> = mutableListOf()) {

    }

    private class Subscriber(val userId: Long, val asyncResponse: AsyncResponse, var lastDispatchedSentId: Long?=null) {

    }


    private var subscribers = mutableMapOf<Long, ChallengeSubscribers>();
    @POST
    @Path("/async/challenges/{challengeId}/events")
    fun listenTo(@Suspended asyncResponse: AsyncResponse, @PathParam("challengeId") challengeId: Long) {
        //TODO permissions

        //TODO inform when more should be fetched??
        //TODO timeouts
        //TODO synchronization
        //TODO fetch only last messages
        // TODO what if in the meantime between remove response and next call somethin new arrived? better than remove, we should set there LAST postId


        val subscriber = Subscriber(session.userId, asyncResponse)

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
    var lastDispatchedEventId:Number?=null;


    // TRANSIENT
    var minEventId:Long?=null;
    var maxEventId:Long?=null;

    fun checkIfSomethingWasDispatchedAfter(minEventIdExclusive: Long?, maxEventIdExclusive:Long?) {
        val minEventIdExclusiveConst=minEventIdExclusive;
        val maxEventIdExclusiveConst=maxEventIdExclusive;
        if (minEventIdExclusiveConst==null) {
            // this should only have place after system start, for example when first person registers to listen
            minEventId=eventGroupDAO.getMaxEventId();
            maxEventId=eventGroupDAO.getMaxEventId();
        }
        if (minEventIdExclusiveConst!=null && maxEventIdExclusiveConst!=null && minEventIdExclusiveConst+1==maxEventIdExclusiveConst) {
            // nothing more for sure
            minEventId=minEventId;
            maxEventId=maxEventIdExclusive;
        } else {
            var notDispatchedEvents=eventGroupDAO.getEventsBetween(minEventIdExclusive, maxEventIdExclusive);
            notDispatchedEvents.forEach {
                broadcastNewMessage(it)
            }
            minEventId=notDispatchedEvents.maxBy({ it.id })?.id;
            maxEventId=null;
        }
    }

    fun broadcastNewMessage(event:EventODB) {
        println("BROADCAST NEW");

        val challengeId=event.challenge.id;
        val postsForTask = eventGroupDAO.getPostsForChallengeWithoutPermissionCheck(challengeId).map(
                { EventDTO.fromODB(it) })
                .toTypedArray();


        val challengeSubscribers: ChallengeSubscribers? = subscribers.get(challengeId);
        if (challengeSubscribers != null) {
            synchronized(challengeSubscribers, {
                challengeSubscribers.users.forEach {

                    it.asyncResponse.resume(EventGroupDTO(challengeId, null, postsForTask))
                    it.lastDispatchedSentId=event.id
                }
                challengeSubscribers.users.clear()
            });
        }
        println("RESUME " + challengeSubscribers?.users?.size)


    }


    @GET
    @Path("/challenges/{challengeId}/events")
    override fun getEventsForChallenge(@PathParam("challengeId") challengeId: Long): EventGroupDTO {
        val callerId = session.getUserId();
        val postsForTask = eventGroupDAO.getPostsForChallenge(callerId, challengeId).map(
                { EventDTO.fromODB(it) })
                .toTypedArray();
        return EventGroupDTO(challengeId, null, postsForTask);
    }

    @POST
    @WebResponseStatus(CREATED)
    @Path("/challenges/{challengeId}/events")
    override fun createEvent(@PathParam("challengeId") challengeId: Long, eventDTO: EventDTO): EventDTO {
        if (challengeId != eventDTO.challengeId)
            throw IllegalArgumentException();
        val callerId = session.getUserId();

        var ev = EventODB(eventDTO.id);
        ev.author = UserODB(eventDTO.authorId);
        ev.challenge = ChallengeODB(eventDTO.challengeId);
        ev.task = if (eventDTO.taskId != null) TaskODB(eventDTO.taskId ?: throw IllegalArgumentException()) else null;
        ev.content = eventDTO.content;
        ev.createDate = Date(eventDTO.sentDate);
        ev.eventType = EventType.POST;


        var updatedEvent = eventGroupDAO.editEvent(callerId, challengeId, ev);
        broadcastNewMessage(updatedEvent)
        return EventDTO.fromODB(updatedEvent);
    }
}