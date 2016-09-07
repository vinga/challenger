package com.kameo.challenger.web.rest.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.logic.ChallengerLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.odb.api.IIdentity;
import com.kameo.challenger.web.rest.ChallengerSess;
import com.kameo.challenger.web.rest.api.IChallengerService;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    public VisibleContractsDTO getVisibleChallengeContracts() {
        long callerId = session.getUserId();
        ChallengerLogic.ChallengeContractInfoDTO cinfo = challengerLogic
                .getVisibleChallengeContracts(callerId);

        VisibleContractsDTO res = new VisibleContractsDTO();
        res.setSelectedContractId(cinfo.getDefaultChallengeId());

        res.setVisibleChallenges(cinfo.getVisibleChallenges().stream()
             .map(VisibleContractsDTO.ChallengeContractDTO::fromODB)
             .map(c->{ c.setMyId(callerId); return c; })
             .collect(Collectors.toList()));

        return res;
    }


    @POST
    @Path("updateChallengeAction")
    public ChallengeActionDTO updateChallengeAction(ChallengeActionDTO challengeActionDTO) {
        long callerId = session.getUserId();
        ChallengeActionODB challengeActionODB = new ChallengeActionODB();
        challengeActionODB.setActionStatus(challengeActionDTO.getActionStatus());
        challengeActionODB.setActionType(challengeActionDTO.getActionType());
        challengeActionODB.setIcon(challengeActionDTO.getIcon());
        challengeActionODB.setLabel(challengeActionDTO.getLabel());
        challengeActionODB.setChallengeContract(new ChallengeContractODB(challengeActionDTO.getContractId()));
        challengeActionODB.setUser(new UserODB(session.getUserId()));
        challengeActionODB.setCreatedByUser(new UserODB(callerId));
        challengeActionODB.setDifficulty(challengeActionDTO.getDifficulty());
        if (challengeActionDTO.getDueDate() != null)
            challengeActionODB.setDueDate(new Date(challengeActionDTO.getDueDate()));
        if (challengeActionDTO.getId() > 0)
            challengeActionODB.setId(challengeActionDTO.getId());
        challengeActionODB = challengerLogic.updateChallengeAction(callerId, challengeActionODB);
        challengeActionODB.setUser(new UserODB(challengeActionDTO.getUserId()));
        return ChallengeActionDTO.fromOdb(challengeActionODB);
    }




    @GET
    @Path("challengeActionsForMe/{contractId}")
    public List<ChallengeActionDTO> getChallengeActionsForMe(@PathParam("contractId") long contractId) {
        long callerId = session.getUserId();
        List<ChallengeActionODB> actions = challengerLogic
                .getChallengeActionsAssignedToPerson(callerId, callerId, contractId);
        return actions.stream().sorted(IIdentity::compare).map(ChallengeActionDTO::fromOdb).collect(Collectors.toList());
    }

    @GET
    @Path("challengeActionsForOther/{contractId}")
    public List<ChallengeActionDTO> getChallengeActionsForOther(@PathParam("contractId") long contractId) {
        long callerId = session.getUserId();
        List<ChallengeActionODB> actions = challengerLogic.getChallengeActionsAssignedToOther(callerId, contractId);
        return actions.stream().sorted(IIdentity::compare).map(ChallengeActionDTO::fromOdb).collect(Collectors.toList());
    }



}