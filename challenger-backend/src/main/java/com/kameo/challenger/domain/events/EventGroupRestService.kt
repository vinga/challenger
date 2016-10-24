package com.kameo.challenger.domain.events

import com.kameo.challenger.domain.accounts.EventGroupDAO
import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.events.IEventGroupRestService.EventDTO.Companion
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.web.rest.ChallengerSess
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path("/api/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EventGroupRestService : IEventGroupRestService {


    @Inject
    private lateinit var eventGroupDAO: EventGroupDAO


    @Inject
    private lateinit var session: ChallengerSess;


    @GET
    @Path("{taskId}")
    override fun getEventsForTask(@PathParam("taskId") taskId: Long): IEventGroupRestService.EventGroupDTO {
        val callerId = session.getUserId();

        val postsForTask = eventGroupDAO.getPostsForTask(callerId, taskId).map(
                { IEventGroupRestService.EventDTO.fromODB(it) })
                .toTypedArray();
        var challengeId=
                if (postsForTask.isNotEmpty())
                    postsForTask.first().challengeId
                else
                    eventGroupDAO.getChallengeIdForTaskId(callerId, taskId);

        return IEventGroupRestService.EventGroupDTO(challengeId, taskId, postsForTask);
    }

    @GET
    @Path("challenge/{challengeId}")
    override fun getEventsForChallenge(@PathParam("challengeId") challengeId: Long): IEventGroupRestService.EventGroupDTO {
        val callerId = session.getUserId();
        val postsForTask = eventGroupDAO.getPostsForChallenge(callerId, challengeId).map(
                { IEventGroupRestService.EventDTO.fromODB(it) })
                .toTypedArray();
        return IEventGroupRestService.EventGroupDTO(challengeId, null, postsForTask);
    }

    @POST
    @Path("sendEvent")
    override fun editEvent(eventDTO: IEventGroupRestService.EventDTO):IEventGroupRestService.EventDTO {
        val callerId = session.getUserId();

        var ev=EventODB(eventDTO.id);
        ev.author= UserODB(eventDTO.authorId);
        ev.challenge= ChallengeODB(eventDTO.challengeId);
        ev.task= if (eventDTO.taskId!=null) TaskODB(eventDTO.taskId ?: throw IllegalArgumentException()) else null;
        ev.content=eventDTO.content;
        ev.createDate= Date(eventDTO.sentDate);
        ev.eventType=EventType.POST;


        var updatedEvent=eventGroupDAO.editPost(callerId, ev);
        return IEventGroupRestService.EventDTO.fromODB(updatedEvent);
    }
}