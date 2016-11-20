package com.kameo.challenger.domain.events

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO
import com.kameo.challenger.domain.events.IEventGroupRestService.EventGroupDTO
import com.kameo.challenger.domain.events.db.EventODB
import com.kameo.challenger.domain.events.db.EventType
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus.CREATED
import com.kameo.challenger.web.rest.ChallengerSess
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.stereotype.Component
import java.util.*
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
    private lateinit var session: ChallengerSess
    @Inject
    private lateinit var eventPushDAO: EventPushDAO


    @Deprecated("use getEventsForChallenge instaed")
    @GET
    @Path("/challenges/{challengeId}/tasks/{taskId}/events")
    @ApiOperation("Gets events for task")
    override fun getEventsForTask(@PathParam("challengeId") challengeId: Long, @PathParam("taskId") taskId: Long): EventGroupDTO {
        val callerId = session.userId

        val postsForTask = eventGroupDAO.getEventsForTask(callerId, challengeId, taskId).map(
                { EventDTO.fromODB(it) })
                .toTypedArray()

        return EventGroupDTO(challengeId, taskId, postsForTask)
    }


    @POST
    @Path("/async/challenges/{challengeId}/events")
    @WebResponseStatus(WebResponseStatus.ACCEPTED)
    fun listenTo(@Suspended asyncResponse: AsyncResponse, @PathParam("challengeId") challengeId: Long, @QueryParam("lastEventId") lastEventId: Long?) {
        eventPushDAO.listenToNewEvents(session.jwtToken, session.userId, asyncResponse, challengeId, lastEventId)
    }

    @POST
    @Path("/challenges/{challengeId}/events/{eventId}/markRead")
    fun markEventRead(@PathParam("challengeId") challengeId: Long, @PathParam("eventId") eventId: Long, readDate: Long) {
        eventGroupDAO.markEventAsRead(session.userId, challengeId, eventId, Date(readDate))
    }


    @GET
    @Path("/challenges/{challengeId}/events")
    override fun getEventsForChallenge(@PathParam("challengeId") challengeId: Long): EventGroupDTO {
        val callerId = session.userId
        val postsForTask = eventGroupDAO.getLastEventsForChallenge(callerId, challengeId).map { EventDTO.fromODB(it) }
                .toTypedArray()
        return EventGroupDTO(challengeId, null, postsForTask)
    }

    @POST
    @WebResponseStatus(CREATED)
    @Path("/challenges/{challengeId}/events")
    override fun createEvent(@PathParam("challengeId") challengeId: Long, eventDTO: EventDTO): EventDTO {
        if (challengeId != eventDTO.challengeId)
            throw IllegalArgumentException()
        val callerId = session.userId

        var ev = EventODB(0)
        ev.author = UserODB(eventDTO.authorId)
        ev.challenge = ChallengeODB(eventDTO.challengeId)
        ev.taskId = eventDTO.taskId
        ev.content = eventDTO.content
        ev.createDate = Date(eventDTO.sentDate)
        ev.eventType = EventType.POST

        var updatedEvent = eventGroupDAO.createEventFromClient(callerId, challengeId, ev)

        return EventDTO.fromODB(updatedEvent)
    }
}