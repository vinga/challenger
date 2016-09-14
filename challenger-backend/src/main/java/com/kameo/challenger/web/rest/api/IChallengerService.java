package com.kameo.challenger.web.rest.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.odb.*;
import lombok.Data;

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
    public static class VisibleChallengesDTO {

        Long selectedChallengeId;

        List<ChallengeDTO> visibleChallenges = Lists.newArrayList();

        @Data
        public static class ChallengeDTO {
            long id;
            String label;
            String challengeStatus;
            long firstUserId;
            long secondUserId;
            String firstUserLabel;
            String secondUserLabel;
            long myId;

            public static ChallengeDTO fromODB(ChallengeODB c) {
                ChallengeDTO co = new ChallengeDTO();
                co.label = c.getLabel();
                co.id = c.getId();
                co.challengeStatus = c.getChallengeStatus().name();
                co.firstUserId = c.getFirst().getId();
                co.secondUserId = c.getSecond().getId();
                co.firstUserLabel = Strings.isNullOrEmpty(c.getFirst().getLogin()) ? c.getFirst().getEmail() : c
                        .getFirst()
                        .getLogin();
                co.secondUserLabel = Strings.isNullOrEmpty(c.getSecond().getLogin()) ? c.getSecond().getEmail() : c
                        .getSecond().getLogin();
                return co;
            }
        }


    }
}
