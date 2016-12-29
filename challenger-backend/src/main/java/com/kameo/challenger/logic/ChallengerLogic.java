package com.kameo.challenger.logic;

import com.google.common.collect.Lists;
import com.kameo.challenger.domain.accounts.AccountDAO;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.domain.events.EventGroupDAO;
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
import org.jetbrains.annotations.NotNull;
import org.jinq.jpa.JPQL;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

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

    public List<TaskODB> getTasks(long callerId, long challengeId, LocalDate date) {
        return taskDao.getTasks(callerId, challengeId, date);
    }

    public UserODB getUserById(long callerId) {
        return anyDao.get(UserODB.class, callerId);
    }


    public List<ChallengeODB> getPendingChallenges(long userId) {
        return anyDao.streamAll(ChallengeParticipantODB.class).where(cp -> cp.getUser().getId() == userId && cp
                .getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                     .select(ChallengeParticipantODB::getChallenge).collect(Collectors.toList());
    }

    public List<String> findUsersWithLoginsStartingWith(String friend) {
        //noinspection ConstantConditions
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
                                             ca.getDueDate().isAfter(LocalDateTime.now()))
                     .collect(Collectors.toList());
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
        // I cannot modify anything now, tasks are immutable
        if (true) {
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






    public List<TaskApprovalODB> getTasksApprovalForRejectedTasks(List<TaskODB> tasks) {
        List<Long> rejectedTaskIds = tasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.rejected).map(TaskODB::getId).collect(Collectors.toList());
        if (rejectedTaskIds.isEmpty())
            return Lists.newArrayList();
        return anyDao.streamAll(TaskApprovalODB.class).where(ta -> rejectedTaskIds.contains(ta.getTask().getId()) && ta.getTaskStatus() == TaskStatus.rejected).collect(Collectors.toList());
    }


}
