package com.kameo.challenger.logic;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.domain.accounts.AccountDAO;
import com.kameo.challenger.domain.accounts.EventGroupDAO;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.domain.tasks.TaskDAO;
import com.kameo.challenger.domain.tasks.db.TaskApprovalODB;
import com.kameo.challenger.domain.tasks.db.TaskODB;
import com.kameo.challenger.domain.tasks.db.TaskProgressODB;
import com.kameo.challenger.domain.tasks.db.TaskStatus;
import com.kameo.challenger.domain.tasks.db.TaskType;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.jpa.JPQL;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Transactional
@Component
public class ChallengerLogic {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    private AnyDAO anyDao;
    @Inject
    private AccountDAO accountDao;


    @Inject
    private TaskDAO taskDao;
    @Inject
    private EventGroupDAO eventGroupDao;


    public List<TaskODB> getTasks(long callerId, long challengeId, Date date) {
        return taskDao.getTasks(callerId, challengeId, date);
    }


    // no permission checked
    public List<TaskProgressODB> getTaskProgress(List<TaskODB> tasks, Date day) {
        Date midnight = DateUtil.getMidnight(day);

        List<Long> taskIds = tasks.stream().map(TaskODB::getId).collect(Collectors.toList());
        if (taskIds.isEmpty())
            return Collections.emptyList();
        return anyDao.streamAll(TaskProgressODB.class)
                     .where(tp ->
                             taskIds.contains(tp.getTask().getId()) && tp.getProgressTime().equals(midnight)
                     )
                     .collect(Collectors.toList());
    }


    public List<ChallengeODB> getPendingChallenges(long userId) {
        return anyDao.streamAll(ChallengeParticipantODB.class).where(cp -> cp.getUser().getId() == userId && cp
                .getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                     .select(ChallengeParticipantODB::getChallenge).collect(Collectors.toList());
    }


    public List<String> findUsersWithLoginsStartingWith(String friend) {
        return anyDao.streamAll(UserODB.class).where(u ->
                JPQL.like(u.getLogin(), friend + "%")
        ).map(UserODB::getLogin).sorted().collect(Collectors.toList());
    }


    public List<TaskODB> getWaitingForAcceptanceTasksForConctract(long userId, long contractId) {
        return anyDao.streamAll(TaskODB.class)
                     .where(ca -> ca.getChallenge().getId() == contractId &&
                             ca.getUser().getId() == userId &&
                             ca.getTaskStatus() == TaskStatus.waiting_for_acceptance &&
                             ca.getChallenge().getChallengeStatus() == ChallengeStatus.ACTIVE)
                     .filter(ca ->
                             ca.getTaskType() != TaskType.onetime ||
                                     ca.getTaskType() == TaskType.onetime &&
                                             new DateTime(ca.getDueDate()).isAfterNow())
                     .collect(Collectors.toList());
    }

    public TaskODB createTask(long callerId, TaskODB taskODB) {
        TaskODB task = taskDao.createTask(callerId, taskODB);
       return task;
    }

    public TaskODB updateTask(long callerId, TaskODB taskODB) {
        ChallengeODB cc = anyDao.get(ChallengeODB.class, taskODB.getChallenge().getId());
        if (!cc.getParticipants().stream().anyMatch(cp -> cp.getUser().getId() == callerId))
            throw new IllegalArgumentException();
        if (!cc.getParticipants().stream().anyMatch(cp -> cp.getUser().getId() == taskODB.getUser().getId()))
            throw new IllegalArgumentException();

        TaskODB caDB = anyDao.get(TaskODB.class, taskODB.getId());
        if (caDB.getUser().getId() != callerId && caDB.getCreatedByUser().getId() != callerId)
            throw new IllegalArgumentException();
        boolean isSelfCreatedForMyself = caDB.getCreatedByUser().getId() == caDB.getUser().getId() && caDB.getUser()
                                                                                                          .getId() == callerId;
        // moge modyfikowac tylko swoje stworzone przez siebie i dla siebie
        if (!isSelfCreatedForMyself) {
            throw new IllegalArgumentException("Cannot modify action that is not waiting for acceptance ");
        }


        caDB.setDueDate(taskODB.getDueDate());
        caDB.setIcon(taskODB.getIcon());
        caDB.setLabel(taskODB.getLabel());
        caDB.setTaskStatus(taskODB.getTaskStatus());
        caDB.setDifficulty(taskODB.getDifficulty());
        caDB.setTaskType(taskODB.getTaskType());
        anyDao.getEm().merge(caDB);
        return caDB;


    }

    public TaskProgressODB markTaskDone(long callerId, long taskId, Date day, boolean done) {
        Date dayMidnight = DateUtil.getMidnight(day);
        TaskODB task = anyDao.get(TaskODB.class, taskId);
        if (task.getUser().getId() != callerId)
            throw new IllegalArgumentException();
        Optional<TaskProgressODB> otp = anyDao.streamAll(TaskProgressODB.class)
                                              .where(t -> t.getTask().getId() == taskId)
                                              .where(t -> t.getProgressTime().equals(dayMidnight))
                                              .findAny();

        if (otp.isPresent()) {
            otp.get().setDone(done);
            anyDao.getEm().merge(otp.get());
            return otp.get();
        } else {
            TaskProgressODB tp = new TaskProgressODB();
            tp.setTask(task);
            tp.setProgressTime(dayMidnight);
            tp.setDone(done);
            anyDao.getEm().merge(tp);
            return tp;
        }


    }



    public ChallengeODB updateChallengeState(long callerId, long challengeId, ChallengeStatus status) {
        if (status != ChallengeStatus.ACTIVE && status != ChallengeStatus.REFUSED)
            throw new IllegalArgumentException();

        ChallengeParticipantODB cpb = anyDao.streamAll(ChallengeParticipantODB.class)
                                            .where(cp -> cp.getUser().getId() == callerId &&
                                                    cp.getChallenge().getId() == challengeId &&
                                                    cp.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                                            .findOne().get();

        cpb.setChallengeStatus(status);
        anyDao.getEm().merge(cpb);
        ChallengeODB challenge = cpb.getChallenge();
        long active = challenge.getParticipants().stream()
                               .filter(cp -> cp.getChallengeStatus() == ChallengeStatus.ACTIVE).count();
        long refused = challenge.getParticipants().stream()
                                .filter(cp -> cp.getChallengeStatus() == ChallengeStatus.REFUSED).count();


        if (active == challenge.getParticipants().size()) {
            challenge.setChallengeStatus(ChallengeStatus.ACTIVE);
            anyDao.getEm().merge(challenge);
        } else if (active + refused == challenge.getParticipants().size() && active > 1) {
            challenge.setChallengeStatus(ChallengeStatus.ACTIVE);
            anyDao.getEm().merge(challenge);
        }
        return challenge;
    }

    public TaskODB changeTaskStatus(long taskId, Set<Long> userIds, TaskStatus taskStatus, String rejectionReason) {
//TODO check challenge permissions
        List<TaskApprovalODB> existingApprovals = anyDao.streamAll(TaskApprovalODB.class)
                                                        .where(ta -> ta.getTask().getId() == taskId)
                                                        .collect(Collectors.toList());


        TaskODB task = anyDao.get(TaskODB.class, taskId);
        ChallengeODB challenge = task.getChallenge();

        if (taskStatus == TaskStatus.waiting_for_acceptance)
            throw new IllegalArgumentException();
        if (taskStatus == TaskStatus.rejected && Strings.isNullOrEmpty(rejectionReason))
            throw new IllegalArgumentException();
        if (task.getTaskStatus() != TaskStatus.waiting_for_acceptance)
            throw new IllegalArgumentException();


        boolean userIdsAreChallengeParticipants = challenge.getParticipants().stream().map(cp -> cp.getUser().getId())
                                                           .collect(Collectors.toSet())
                                                           .containsAll(userIds);
        if (!userIdsAreChallengeParticipants)
            throw new IllegalArgumentException();


        challenge.getParticipants().stream().map(ChallengeParticipantODB::getUser).forEach(u -> {
            if (userIds.contains(u.getId())) {
                Optional<TaskApprovalODB> ota = existingApprovals.stream()
                                                                 .filter(ta -> ta.getUser().getId() == u.getId())
                                                                 .findAny();
                if (ota.isPresent()) {
                    TaskApprovalODB ta = ota.get();
                    ta.setRejectionReason(rejectionReason);
                    ta.setTaskStatus(taskStatus);
                    anyDao.getEm().merge(ta);
                } else {
                    TaskApprovalODB ta = new TaskApprovalODB();
                    ta.setTask(task);
                    ta.setUser(u);
                    ta.setRejectionReason(rejectionReason);
                    ta.setTaskStatus(taskStatus);
                    anyDao.getEm().persist(ta);
                    existingApprovals.add(ta);
                }

            }
        });
        if (taskStatus == TaskStatus.rejected) {
            task.setTaskStatus(TaskStatus.rejected);
            anyDao.getEm().merge(task);
        }

        boolean allParticipantsAccepted = existingApprovals.stream().filter(ta -> ta.getTaskStatus() == TaskStatus.accepted)
                                                           .count() == challenge.getParticipants().size();

        if (allParticipantsAccepted) {
            task.setTaskStatus(TaskStatus.accepted);
            anyDao.getEm().merge(task);
        }
        return task;
    }

    public List<TaskApprovalODB> getTasksApprovalForRejectedTasks(List<TaskODB> tasks) {
        List<Long> rejectedTaskIds = tasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.rejected).map(TaskODB::getId).collect(Collectors.toList());
        if (rejectedTaskIds.isEmpty())
            return Lists.newArrayList();
        return anyDao.streamAll(TaskApprovalODB.class).where(ta -> rejectedTaskIds.contains(ta.getTask().getId()) && ta.getTaskStatus() == TaskStatus.rejected).collect(Collectors.toList());
    }


}
