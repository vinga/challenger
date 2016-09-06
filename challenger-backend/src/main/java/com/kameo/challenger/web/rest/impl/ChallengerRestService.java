package com.kameo.challenger.web.rest.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.ReflectionUtils;
import com.kameo.challenger.web.rest.ChallengerSess;
import com.kameo.challenger.web.rest.api.ITestService;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;


@Component
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChallengerRestService implements ITestService {
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
    public VisibleContractsDTO getVisibleChallengeContracts() {
        long callerId = session.getUserId();
        ChallengerLogic.ChallengeContractInfoDTO cinfo = challengerLogic
                .getVisibleChallengeContracts(callerId);

        VisibleContractsDTO res = new VisibleContractsDTO();
        res.setSelectedContractId(cinfo.getDefaultChallengeId());
        cinfo.getVisibleChallenges().forEach(c -> {
            VisibleContractsDTO.ChallengeContractDTO co = new VisibleContractsDTO.ChallengeContractDTO();
            co.label = c.getLabel();
            co.id = c.getId();
            co.myId=callerId;
            co.challengeContractStatus = c.getChallengeContractStatus().name();
            co.firstUserId = c.getFirst().getId();
            co.secondUserId = c.getSecond().getId();
            co.firstUserLabel = Strings.isNullOrEmpty(c.getFirst().getLogin()) ? c.getFirst().getEmail() : c.getFirst()
                                                                                                            .getLogin();
            co.secondUserLabel = Strings.isNullOrEmpty(c.getSecond().getLogin()) ? c.getSecond().getEmail() : c
                    .getSecond().getLogin();
            res.getVisibleChallenges().add(co);
        });
        return res;
    }


    @POST
    @Path("updateChallenge")
    public void updateChallenge(ChallengeActionDTO challengeActionDTO) {
        System.out.println("updatechallenge... ......");
        System.out.println("UPDATDE challenge "+new Gson().toJson(challengeActionDTO));
        ChallengeActionODB challengeActionODB = new ChallengeActionODB();
        challengeActionODB.setActionStatus(challengeActionDTO.getActionStatus());
        challengeActionODB.setActionType(challengeActionDTO.getActionType());
        challengeActionODB.setLabel(challengeActionDTO.getLabel());
        challengeActionODB.setChallengeContract(new ChallengeContractODB(challengeActionDTO.getContractId()));
        challengeActionODB.setUser(new UserODB(session.getUserId()));
        challengeActionODB.setCreatedByUser(new UserODB(session.getUserId()));
        challengeActionODB.setDifficulty(challengeActionDTO.getDifficulty());
        challengeActionODB.setDueDate(new Date(challengeActionDTO.getDueDate()));
        challengeActionODB.setId(challengeActionDTO.getId());
    }


    @Data
    public static class VisibleContractsDTO {

        Long selectedContractId;

        List<ChallengeContractDTO> visibleChallenges = Lists.newArrayList();

        @Data
        public static class ChallengeContractDTO {
            long id;
            String label;
            String challengeContractStatus;
            long firstUserId;
            long secondUserId;
            String firstUserLabel;
            String secondUserLabel;
            long myId;
        }
    }

    @GET
    @Path("challengeActionsForMe/{contractId}")
    public List<ChallengeActionDTO> getChallengeActionsForMe(@PathParam("contractId") long contractId) {
        //System.out.println("found userid " + session.getUserId());
        long callerId = session.getUserId();
        List<ChallengeActionODB> actions = challengerLogic.getChallengeActionsAssignedToPerson(callerId, callerId, contractId);
        List<ChallengeActionDTO> ua = ReflectionUtils.copyList(actions, ChallengeActionDTO.class);

        List<ChallengeActionDTO> res=Lists.newArrayList();
        for (int i=0; i<20; i++) {
            ChallengeActionDTO r = new ChallengeActionDTO();
            r.id = i;
            r.icon = "fa-book";
            r.difficulty = i%3;
            r.label = "Example task "+i+" " + contractId;
            r.actionType = ActionType.monthly;
            r.actionStatus = ActionStatus.done;
            res.add(r);
        }


        return res;
    }

    @GET
    @Path("challengeActionsForOther/{contractId}")
    public List<ChallengeActionDTO> getChallengeActionsForOther(@PathParam("contractId") long contractId) {
        //System.out.println("found userid " + session.getUserId());
        long callerId = session.getUserId();
        List<ChallengeActionODB> actions = challengerLogic.getChallengeActionsAssignedToOther(callerId, contractId);
        List<ChallengeActionDTO> ua = ReflectionUtils.copyList(actions, ChallengeActionDTO.class);
       // return ua;

        List<ChallengeActionDTO> res=Lists.newArrayList();
        for (int i=0; i<2; i++) {
            ChallengeActionDTO r = new ChallengeActionDTO();
            r.id = i;
            r.icon = "fa-car";
            r.difficulty = i%3;
            r.label = "Example task "+i+" " + contractId;
            r.actionType = ActionType.monthly;
            r.actionStatus = ActionStatus.done;
            res.add(r);
        }


        return res;
    }


    @Data
    public static class ChallengeActionDTO {
        long id;
        String label;
        String icon;
        int difficulty;
        long contractId;
        Long dueDate;

        ActionType actionType;
        ActionStatus actionStatus;

    }

}