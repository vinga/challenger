package com.kameo.challenger.web.rest.impl;

import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.odb.api.IIdentity;
import com.kameo.challenger.web.rest.ChallengerSess;
import com.kameo.challenger.web.rest.api.IChallengerService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChallengerRestService implements IChallengerService {
    @Inject
    private ChallengerSess session;


    @Inject
    private ChallengerLogic challengerLogic;

    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }


    @GET
    @Path("visibleChallenges")
    public VisibleChallengesDTO getVisibleChallenges() {
        long callerId = session.getUserId();
        ChallengerLogic.ChallengeInfoDTO cinfo = challengerLogic
                .getVisibleChallenges(callerId);

        VisibleChallengesDTO res = new VisibleChallengesDTO();
        res.setSelectedChallengeId(cinfo.getDefaultChallengeId());

        res.setVisibleChallenges(cinfo.getVisibleChallenges().stream()
             .map(VisibleChallengesDTO.ChallengeDTO::fromODB)
             .map(c->{ c.setMyId(callerId); return c; })
             .collect(Collectors.toList()));

        return res;
    }


    @POST
    @Path("updateTask")
    public TaskDTO updateTask(TaskDTO challengeTaskDTO) {
        long callerId = session.getUserId();
        TaskODB challengeTaskODB = new TaskODB();
        challengeTaskODB.setTaskStatus(challengeTaskDTO.getTaskStatus());
        challengeTaskODB.setTaskType(challengeTaskDTO.getTaskType());
        challengeTaskODB.setIcon(challengeTaskDTO.getIcon());
        challengeTaskODB.setLabel(challengeTaskDTO.getLabel());
        challengeTaskODB.setChallenge(new ChallengeODB(challengeTaskDTO.getChallengeId()));
        challengeTaskODB.setUser(new UserODB(session.getUserId()));
        challengeTaskODB.setCreatedByUser(new UserODB(callerId));
        challengeTaskODB.setDifficulty(challengeTaskDTO.getDifficulty());
        if (challengeTaskDTO.getDueDate() != null)
            challengeTaskODB.setDueDate(new Date(challengeTaskDTO.getDueDate()));
        if (challengeTaskDTO.getId() > 0)
            challengeTaskODB.setId(challengeTaskDTO.getId());
        challengeTaskODB = challengerLogic.updateTask(callerId, challengeTaskODB);
        challengeTaskODB.setUser(new UserODB(challengeTaskDTO.getUserId()));
        return TaskDTO.fromOdb(challengeTaskODB);
    }




    @GET
    @Path("tasksForMe/{contractId}")
    public List<TaskDTO> getChallengeTasksForMe(@PathParam("contractId") long contractId) {
        long callerId = session.getUserId();
        List<TaskODB> Tasks = challengerLogic
                .getTasksAssignedToPerson(callerId, callerId, contractId);


        return Tasks.stream().sorted(IIdentity::compare).map(TaskDTO::fromOdb).collect(Collectors.toList());
    }

    @GET
    @Path("tasksForOther/{contractId}")
    public List<TaskDTO> getChallengeTasksForOther(@PathParam("contractId") long contractId) {
        long callerId = session.getUserId();
        List<TaskODB> Tasks = challengerLogic.getTasksAssignedToOther(callerId, contractId);
        return Tasks.stream().sorted(IIdentity::compare).map(TaskDTO::fromOdb).collect(Collectors.toList());
    }



}