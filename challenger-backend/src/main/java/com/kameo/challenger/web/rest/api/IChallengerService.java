package com.kameo.challenger.web.rest.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.odb.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
public interface IChallengerService {
    @Data
    public static class TaskProgressDTO {
        long taskId;
        long progressTime;
        boolean done;
        public static TaskProgressDTO fromOdb(TaskProgressODB odb) {
            TaskProgressDTO tp=new TaskProgressDTO();
            tp.setProgressTime(odb.getProgressTime().getTime());
            tp.setTaskId(odb.getTask().getId());
            tp.setDone(odb.isDone());
            return tp;
        }

    }

    @Data
    public static class TaskApprovalDTO {
        long userId;
        public long taskId;
        TaskStatus taskStatus;
        String rejectionReason;

        public static IChallengerService.TaskApprovalDTO fromODBtoDTO(TaskApprovalODB odb) {
            TaskApprovalDTO dto = new TaskApprovalDTO();
            dto.setTaskId(odb.getTask().getId());
            dto.setRejectionReason(odb.getRejectionReason());
            dto.setTaskStatus(odb.getTaskStatus());
            dto.setUserId(odb.getUser().getId());
            return dto;
        }
    }

    @Data
    public static class TaskDTO {
        long id;
        String label;
        String icon;
        int difficulty; //0-2
        long challengeId;
        Long dueDate;
        long userId;
        long createdByUserId;
        TaskType taskType;
        TaskStatus taskStatus;
        boolean done;
        Boolean deleted;
        TaskApprovalDTO taskApproval;

        public static TaskDTO fromOdb(TaskODB odb) {
            TaskDTO ca = new TaskDTO();
            ca.id = odb.getId();
            ca.label = odb.getLabel();
            ca.icon = odb.getIcon();
            ca.difficulty = odb.getDifficulty();
            ca.challengeId = odb.getChallenge().getId();
            ca.dueDate = Optional.ofNullable(odb.getDueDate()).map(d -> d.getTime()).orElse(null);
            ca.taskType = odb.getTaskType();
            ca.taskStatus = odb.getTaskStatus();
            ca.userId = odb.getUser().getId();
            ca.done=odb.isDone();
            ca.createdByUserId=odb.getCreatedByUser().getId();
            return ca;
        }


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLabelDTO {
        long id;
        String label;
        String login;//if exists
    }

    @Data
    public static class VisibleChallengesDTO {

        Long selectedChallengeId;

        List<ChallengeDTO> visibleChallenges = Lists.newArrayList();


        @Data
        public static class ChallengeDTO {
            long id;
            String label;
            String challengeStatus;
            long creatorId;


            long myId;
            UserLabelDTO[] userLabels;

            public static ChallengeDTO fromODB(ChallengeODB c) {
                ChallengeDTO co = new ChallengeDTO();
                co.label = c.getLabel();
                co.id = c.getId();
                co.challengeStatus = c.getChallengeStatus().name();
                co.creatorId = c.getCreatedBy().getId();
                //co.secondUserId = c.getSecond().getId();


                co.userLabels=c.getParticipants().stream().map(cp->cp.getUser()).map(u->new UserLabelDTO(u.getId(), u.getLoginOrEmail(), u.getLogin())).toArray(UserLabelDTO[]::new);
            /*    List<UserODB> users=Lists.newArrayList(c.getFirst(), c.getSecond());

                co.userLabels= users.stream().map(u->new UserLabelDTO(u.getId(), u.getLogin()!=null? u.getLogin():u.getEmail(), u.getLogin())).toArray(UserLabelDTO[]::new);
*/
               /* co.firstUserLabel = Strings.isNullOrEmpty(c.getFirst().getLogin()) ? c.getFirst().getEmail() : c
                        .getFirst()
                        .getLogin();
                co.secondUserLabel = Strings.isNullOrEmpty(c.getSecond().getLogin()) ? c.getSecond().getEmail() : c
                        .getSecond().getLogin();*/
                return co;
            }
        }


    }
}
