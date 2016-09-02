package com.kameo.challenger.web.rest.impl;

import com.google.common.collect.Lists;
import com.kameo.challenger.odb.ActionStatus;
import com.kameo.challenger.odb.ActionType;
import com.kameo.challenger.odb.UserODB;
import com.kameo.challenger.logic.UserRepo;
import com.kameo.challenger.utils.ReflectionUtils;
import com.kameo.challenger.web.rest.ChallengerSess;
import com.kameo.challenger.web.rest.api.ITestService;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Component
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChallengerRestService implements ITestService {
    @Inject
    private ChallengerSess session;


    @Inject
    private UserRepo userRepo;

    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }


    @GET
    @Path("getUser/{login}")
    public User getUserByLogin(@PathParam("login") String login) {
        System.out.println("login " + login);
        UserODB userOdb = userRepo.getUserByLogin(login);
        return ReflectionUtils.copy(userOdb, User.class);
    }



    @GET
    @Path("challengeActions")
    public List<UserActionDTO> getUserActions() {
        System.out.println("found userid "+session.getUserId());
        UserActionDTO r = new UserActionDTO();
        r.id = 3;
        r.icon = "fa-book";
        r.difficulty = 0;
        r.actionName = "Example task 1";
        r.actionType = ActionType.monthly;
        r.actionStatus = ActionStatus.done;
        return Lists.newArrayList(r);
    }


    @Data
    public static class UserActionDTO {
        long id;
        String icon;
        int difficulty;
        String actionName;
        ActionType actionType;
        ActionStatus actionStatus;

    }

}