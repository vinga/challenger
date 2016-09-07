package com.kameo.challenger.web.rest.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.odb.ActionStatus;
import com.kameo.challenger.odb.ActionType;
import com.kameo.challenger.odb.ChallengeActionODB;
import com.kameo.challenger.odb.ChallengeContractODB;
import lombok.Data;

import java.util.List;
import java.util.Optional;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
public interface IChallengerService {


    @Data
    public static class ChallengeActionDTO {
        long id;
        String label;
        String icon;
        int difficulty; //0-2
        long contractId;
        Long dueDate;
        long userId;
        ActionType actionType;
        ActionStatus actionStatus;

        public static ChallengeActionDTO fromOdb(ChallengeActionODB odb) {
            ChallengeActionDTO ca = new ChallengeActionDTO();
            ca.id = odb.getId();
            ca.label = odb.getLabel();
            ca.icon = odb.getIcon();
            ca.difficulty = odb.getDifficulty();
            ca.contractId = odb.getChallengeContract().getId();
            ca.dueDate = Optional.ofNullable(odb.getDueDate()).map(d->d.getTime()).orElse(null);
            ca.actionType = odb.getActionType();
            ca.actionStatus = odb.getActionStatus();
            ca.userId=odb.getUser().getId();
            return ca;
        }

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

            public static ChallengeContractDTO fromODB(ChallengeContractODB c) {
                ChallengeContractDTO co = new ChallengeContractDTO();
                co.label = c.getLabel();
                co.id = c.getId();
                co.challengeContractStatus = c.getChallengeContractStatus().name();
                co.firstUserId = c.getFirst().getId();
                co.secondUserId = c.getSecond().getId();
                co.firstUserLabel = Strings.isNullOrEmpty(c.getFirst().getLogin()) ? c.getFirst().getEmail() : c.getFirst()
                                                                                                                .getLogin();
                co.secondUserLabel = Strings.isNullOrEmpty(c.getSecond().getLogin()) ? c.getSecond().getEmail() : c
                        .getSecond().getLogin();
                return co;
            }
        }



    }
}
