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
import java.util.concurrent.TimeUnit
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
    @Inject
    private lateinit var eventPushDAO: EventPushDAO


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





    @POST
    @Path("/async/challenges/{challengeId}/events")
    @WebResponseStatus(WebResponseStatus.ACCEPTED)
    fun listenTo(@Suspended asyncResponse: AsyncResponse, @PathParam("challengeId") challengeId: Long) {
        //TODO permissions

        //TODO inform when more should be fetched??
        //TODO timeouts
        //TODO synchronization
        //TODO fetch only last messages
        // TODO what if in the meantime between remove response and next call somethin new arrived? better than remove, we should set there LAST postId

        eventPushDAO.listenToNewEvents(session.userId, asyncResponse, challengeId);

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
        eventPushDAO.broadcastNewEvent(updatedEvent)
        return EventDTO.fromODB(updatedEvent);
    }
}