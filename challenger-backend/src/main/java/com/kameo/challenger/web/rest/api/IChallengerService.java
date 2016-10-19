package com.kameo.challenger.web.rest.api;

import com.google.common.collect.Lists;
import com.kameo.challenger.domain.challenges.ChallengeODB;
import com.kameo.challenger.odb.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
            tp.setDone(odb.getDone());
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
            ca.done=odb.getDone();
            ca.createdByUserId=odb.getCreatedByUser().getId();
            return ca;
        }


    }



}
