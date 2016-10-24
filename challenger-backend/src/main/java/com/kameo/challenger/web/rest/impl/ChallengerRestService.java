package com.kameo.challenger.web.rest.impl;


import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.ChallengeDAO;
import com.kameo.challenger.domain.challenges.IChallengeRestService;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.tasks.db.TaskApprovalODB;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.domain.tasks.db.TaskProgressODB;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.api.IIdentity;
import com.kameo.challenger.web.rest.ChallengerSess;
import com.kameo.challenger.web.rest.MultiUserChallengerSess;
import com.kameo.challenger.web.rest.api.IChallengerService;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;


@Component
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChallengerRestService implements IChallengerService {
    @Inject
    private ChallengerSess session;


    @Inject
    Provider<MultiUserChallengerSess> multiSessions;


    @Inject
    private ChallengerLogic challengerLogic;



    @GET
    @Path("ping")
    public String ping() {


        challengerLogic.getTasks(0L,0L,new Date());
        return "pong";
    }




    @POST
    @Path("updateTaskStatus")
    public TaskDTO changeTaskStatus(TaskApprovalDTO ta) {

        Set<Long> userIds = multiSessions.get().getUserIds();
        TaskODB taskODB = challengerLogic.changeTaskStatus(ta.taskId, userIds, ta.getTaskStatus(), ta.getRejectionReason());
        return TaskDTO.fromOdb(taskODB);
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
        if (challengeTaskDTO.getDeleted() != null) {
            challengerLogic.deleteTask(callerId, challengeTaskODB);
            return null;
        }

        challengeTaskODB = challengerLogic.updateTask(callerId, challengeTaskODB);
        challengeTaskODB.setUser(new UserODB(challengeTaskDTO.getUserId()));
        return TaskDTO.fromOdb(challengeTaskODB);
    }

    @POST
    @Path("updateTaskProgress")
    public TaskProgressDTO updateTaskProgress(TaskProgressDTO tp) {
        long callerId = session.getUserId();
        TaskProgressODB tpOdb = challengerLogic
                .markTaskDone(callerId, tp.getTaskId(), new Date(tp.getProgressTime()), tp.isDone());

        return TaskProgressDTO.fromOdb(tpOdb);
    }




    //TODO get all tasks, not only assigned to one person
    @GET
    @Path("tasks/{challengeId}/{date_yy-MM-dd}")
    public List<TaskDTO> getTasks(@PathParam("challengeId") long contractId, @PathParam("date_yy-MM-dd") String dateString) {
        long callerId = session.getUserId();
        Date date = DateTimeFormat.forPattern("yy-MM-dd").parseDateTime(dateString).toDate();


        List<TaskODB> tasks = challengerLogic
                .getTasks(callerId, contractId, date);

        List<TaskApprovalODB> taskApprovalODBs = challengerLogic.getTasksApprovalForRejectedTasks(tasks);

        List<TaskDTO> taskDTOs = tasks.stream().sorted(IIdentity.Meta::compare).map(TaskDTO::fromOdb).collect(Collectors.toList());


        taskApprovalODBs.forEach(taskApprovalODB ->
                taskDTOs.stream().filter(taskDTO -> taskDTO.getId() == taskApprovalODB.getTask().getId())
                        .forEach(taskDTO ->
                                taskDTO.setTaskApproval(TaskApprovalDTO.fromODBtoDTO(taskApprovalODB))));

        return taskDTOs;
    }


}