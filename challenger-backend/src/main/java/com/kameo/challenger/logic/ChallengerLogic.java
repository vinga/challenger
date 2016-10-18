package com.kameo.challenger.logic;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.odb.AnyDAO;
import com.kameo.challenger.utils.odb.EntityHelper;
import com.kameo.challenger.utils.odb.IRestrictions;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JPQL;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Transactional
@Component
public class ChallengerLogic {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    private AnyDAO anyDao;
    @Inject
    private LoginLogic loginLogic;

    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;

    @Inject
    private TaskDAO taskDao;


    public List<TaskODB> getTasks(long callerId, long challengeId, Date date) {
        System.out.println("Z kotliny");
    if (true)
       return taskDao.getTasks(callerId, challengeId,date);


        ChallengeParticipantODB callerPermission = anyDao
                .getOnlyOne(ChallengeParticipantODB.class, cp -> cp.getChallenge().getId() == challengeId && cp
                        .getUser().getId() == callerId);

        updateSeendDateOfChallegeContract(callerPermission);
        List<TaskODB> res = anyDao.streamAll(TaskODB.class)
                                  .where(t -> t.getChallenge().getId() == challengeId)
                                  .where(ca -> ca.getChallenge().getId() == challengeId)
                                  .filter(ca ->
                                          ca.getTaskType() != TaskType.onetime ||
                                                  ca.getTaskType() == TaskType.onetime &&
                                                          new DateTime(ca.getDueDate()).isAfter(date.getTime()))
                                  .collect(Collectors.toList());

        getTaskProgress(res, date).stream().forEach(
                tp -> res.stream().filter(task -> task.getId() == tp.getTask().getId()).findAny().get()
                         .setDone(tp.isDone())
        );





        return res;
    }

    @Deprecated // use getTasks
    public List<TaskODB> getTasksAssignedToPerson(long callerId, long userId, long challengeId, Date date) {
        // permission check
        ChallengeParticipantODB callerPermission = anyDao
                .getOnlyOne(ChallengeParticipantODB.class, cp -> cp.getChallenge().getId() == challengeId && cp
                        .getUser().getId() == callerId);
        if (callerId != userId)
            anyDao.getOnlyOne(ChallengeParticipantODB.class, cp -> cp.getChallenge().getId() == challengeId && cp
                    .getUser().getId() == userId);


        if (callerId == userId)
            updateSeendDateOfChallegeContract(callerPermission);
        List<TaskODB> res = anyDao.streamAll(TaskODB.class)
                                  .where(t -> t.getChallenge().getId() == challengeId)
                                  .where(ca -> ca.getChallenge().getId() == challengeId &&
                                          ca.getUser().getId() == userId)
                                  .filter(ca ->
                                          ca.getTaskType() != TaskType.onetime ||
                                                  ca.getTaskType() == TaskType.onetime &&
                                                          new DateTime(ca.getDueDate()).isAfter(date.getTime()))
                                  .collect(Collectors.toList());

        getTaskProgress(res, date).stream().forEach(
                tp -> res.stream().filter(task -> task.getId() == tp.getTask().getId()).findAny().get()
                         .setDone(tp.isDone())
        );





        return res;

    }


    private void updateSeendDateOfChallegeContract(ChallengeParticipantODB cp) {
        cp.setLastSeen(new Date());
        anyDao.getEm().merge(cp);
    }

    // no permission checked
    public List<TaskProgressODB> getTaskProgress(List<TaskODB> tasks, Date day) {
        Date midnight = DateUtil.getMidnight(day);

        List<Long> taskIds = tasks.stream().map(t -> t.getId()).collect(Collectors.toList());
        if (taskIds.isEmpty())
            return Collections.emptyList();
        return anyDao.streamAll(TaskProgressODB.class)
                     .where(tp ->
                             taskIds.contains(tp.getTask().getId()) && tp.getProgressTime().equals(midnight)
                     )
                     .collect(Collectors.toList());
    }




    public void createNewChallenge(long userId, ChallengeODB cb) {


        if (!cb.getParticipants().stream().anyMatch(cp -> cp.getUser().getId() == userId)) {
            ChallengeParticipantODB cp = new ChallengeParticipantODB();
            cp.setUser(new UserODB(userId));
            cp.setChallenge(cb);
            cp.setChallengeStatus(ChallengeStatus.ACTIVE);
        }

        UserODB challengeCreator = anyDao
                .reload(cb.getParticipants().stream().filter(p -> p.getUser().getId() == userId).findAny().get()
                          .getUser());
        if (cb.getParticipants().size() == 1)
            throw new IllegalArgumentException();
        List<ChallengeParticipantODB> cpToSendEmails = Lists.newArrayList();
        for (ChallengeParticipantODB cp : cb.getParticipants()) {
            UserODB u = cp.getUser();
            // creator has accepted challenge by default
            if (u.getId() == userId) {
                cp.setChallengeStatus(ChallengeStatus.ACTIVE);
                continue;
            }
            cp.setChallengeStatus(ChallengeStatus.WAITING_FOR_ACCEPTANCE);
            if (u.isNew()) {
                if (u.getEmail() == null)
                    throw new IllegalArgumentException("Either second user id or second  user email must be provided");
                Optional<UserODB> osecond = findUserByEmail(u.getEmail());


                if (osecond.isPresent()) {
                    cp.setUser(osecond.get());

                } else {
                    UserODB user = loginLogic.createPendingUserWithEmailOnly(cb);
                    cp.setUser(user);
                }
                cpToSendEmails.add(cp);
            }
        }
        if (Strings.isNullOrEmpty(cb.getLabel())) {
            String newLabel = cb.getParticipants().stream()
                                .map(p -> p.getUser().getLoginOrEmail())
                                .collect(Collectors.joining(", ")).toLowerCase();
            cb.setLabel(newLabel);
        }
        cb.setChallengeStatus(ChallengeStatus.WAITING_FOR_ACCEPTANCE);
        cb.setCreatedBy(challengeCreator);
        anyDao.getEm().persist(cb);
        for (ChallengeParticipantODB cp : cb.getParticipants()) {
            anyDao.getEm().persist(cp);
            if (cpToSendEmails.contains(cp)) {
                confirmationLinkLogic.createAndSendChallengeConfirmationLink(cb, cp);
            }
        }


    }


    public List<ChallengeODB> getPendingChallenges(long userId) {
        return anyDao.streamAll(ChallengeParticipantODB.class).where(cp -> cp.getUser().getId() == userId && cp
                .getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                     .select(cp -> cp.getChallenge()).collect(Collectors.toList());
    }

  /*  public void createNewChallengeAction(long userId, TaskODB ca) {
        ChallengeODB cc = ca.getChallenge();
        ChallengeODB ccDB = anyDao.reload(cc);
        if (ca.getIcon() == null)
            ca.setIcon("fa-car");
        if (ca.getUser() != null && ca.getUser().getId() != ccDB.getFirst().getId() && ca.getUser().getId() != ccDB
                .getSecond().getId())
            throw new RuntimeException("Unauthorized");
        if (ccDB.getFirst().getId() != userId && ccDB.getSecond().getId() != userId)
            throw new RuntimeException("Unauthorized");

        if (ca.getUser() == null || ca.getUser().getId() != userId)
            ca.setActionStatus(ActionStatus.waiting_for_acceptance);
        else if (ca.getActionStatus() == null)
            ca.setActionStatus(ActionStatus.pending);
        ca.setCreatedByUser(new UserODB(userId));
        anyDao.getEm().persist(ca);


    }
*/

    public List<String> findUsersWithLoginsStartingWith(String friend) {
        return anyDao.streamAll(UserODB.class).where(u ->
                JPQL.like(u.getLogin(), friend + "%")
        ).map(u -> u.getLogin()).sorted().collect(Collectors.toList());
    }

    private Optional<UserODB> findUserByEmail(String email) {
        return anyDao.getOne(UserODB.class, u -> u.getEmail().equals(email));
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


    private void updateChallengeLastSeen(long callerId, long challengeId) {

        ChallengeParticipantODB cpa = anyDao.streamAll(ChallengeParticipantODB.class)
                                            .where(cp -> cp.getUser().getId() == callerId && cp.getChallenge()
                                                                                               .getId() == challengeId)
                                            .getOnlyValue();
        cpa.setLastSeen(new Date());
        anyDao.getEm().merge(cpa);

    }

    public TaskODB updateTask(long callerId, TaskODB taskODB) {
        if (taskODB.getId() > 0) {
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
        } else {
            ChallengeODB cc = anyDao.get(ChallengeODB.class, taskODB.getChallenge().getId());
            if (!cc.getParticipants().stream().anyMatch(cp -> cp.getUser().getId() == callerId))
                throw new IllegalArgumentException();
            if (!cc.getParticipants().stream().anyMatch(cp -> cp.getUser().getId() == taskODB.getUser().getId()))
                throw new IllegalArgumentException();


            taskODB.setTaskStatus(TaskStatus.waiting_for_acceptance);
            if (taskODB.getUser().getId() == callerId)
                taskODB.setTaskStatus(TaskStatus.accepted);


            taskODB.setCreatedByUser(new UserODB(callerId));
            anyDao.getEm().persist(taskODB);


            TaskApprovalODB ta = new TaskApprovalODB();
            ta.setUser(taskODB.getCreatedByUser());
            ta.setTask(taskODB);
            ta.setTaskStatus(TaskStatus.accepted);
            anyDao.getEm().merge(ta);

            return taskODB;
        }
    }

    public TaskProgressODB markTaskDone(long callerId, long taskId, Date day, boolean done) {
        Date dayMidnight = DateUtil.getMidnight(day);
        TaskODB task = anyDao.get(TaskODB.class, taskId);
        if (task.getUser().getId() != callerId)
            throw new IllegalArgumentException();
        System.out.println("MARK DONE " + done);
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

    public void deleteTask(long callerId, TaskODB incomingTaskToDelete) {
        TaskODB task = anyDao.get(TaskODB.class, incomingTaskToDelete.getId());
        if (task.getUser().getId() != callerId)
            throw new IllegalArgumentException();

        long taskId = task.getId();
        anyDao.streamAll(TaskProgressODB.class)
              .where(t -> t.getTask().getId() == taskId)
              .forEach(tp -> anyDao.getEm().remove(tp));
        anyDao.getEm().remove(task);

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
        if (task.getTaskStatus()!=TaskStatus.waiting_for_acceptance)
            throw new IllegalArgumentException();


        boolean userIdsAreChallengeParticipants = challenge.getParticipants().stream().map(cp -> cp.getUser().getId())
                                                           .collect(Collectors.toSet())
                                                           .containsAll(userIds);
        if (!userIdsAreChallengeParticipants)
            throw new IllegalArgumentException();


        challenge.getParticipants().stream().map(cp -> cp.getUser()).forEach(u -> {
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
        if (taskStatus==TaskStatus.rejected) {
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
        List<Long> rejectedTaskIds = tasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.rejected).map(t -> t.getId()).collect(Collectors.toList());
        if(rejectedTaskIds.isEmpty())
            return Lists.newArrayList();
        return anyDao.streamAll(TaskApprovalODB.class).where(ta->rejectedTaskIds.contains(ta.getTask().getId()) && ta.getTaskStatus() == TaskStatus.rejected).collect(Collectors.toList());
    }


    public static class ChallengeInfoDTO {
        @Getter
        Long defaultChallengeId;
        @Getter
        List<ChallengeODB> visibleChallenges = Lists.newArrayList();
    }

    public ChallengeInfoDTO getVisibleChallenges(long callerId) {
        ChallengeInfoDTO res = new ChallengeInfoDTO();
        JPAJinqStream<ChallengeParticipantODB> userIsPariticipating = anyDao.streamAll(ChallengeParticipantODB.class)
                                                                            .where(cp -> cp.getUser()
                                                                                           .getId() == callerId &&

                                                                                    (cp.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE || cp
                                                                                            .getChallengeStatus() == ChallengeStatus.ACTIVE));

        res.visibleChallenges = userIsPariticipating.select(c -> c.getChallenge()).collect(Collectors.toList());

        Map<Long, UserODB> map = Maps.newHashMap();
        for (ChallengeODB vc : res.visibleChallenges) {
            EntityHelper.initializeCollection(vc.getParticipants());
          /*  for (ChallengeParticipantODB cp: vc.getParticipants()) {
                map.put(cp.getUser().getId(), cp.getUser());
            }*/
        }


        UserODB user = anyDao.get(UserODB.class, callerId);
        res.defaultChallengeId = user.getDefaultChallengeContract();

        if (res.defaultChallengeId == null && !res.visibleChallenges.isEmpty()) {
            Map<ChallengeODB, ChallengeParticipantODB> collect = userIsPariticipating
                    .collect(Collectors
                            .toMap(ChallengeParticipantODB::getChallenge, Function.identity(), (p1, p2) -> p1));
            res.defaultChallengeId = res.visibleChallenges.stream()
                                                          .sorted(new Comparator<ChallengeODB>() {

                                                              @Override
                                                              public int compare(ChallengeODB o1, ChallengeODB o2) {
                                                                  Date d1 = collect.get(o1).getLastSeen();
                                                                  Date d2 = collect.get(o2).getLastSeen();
                                                                  if (d1 == null)
                                                                      return 1;
                                                                  if (d2 == null)
                                                                      return -1;

                                                                  return -d1.compareTo(d2);
                                                              }
                                                          })
                                                          .filter(c -> c.getChallengeStatus() == ChallengeStatus.ACTIVE)
                                                          .findAny()
                                                          .orElse(res.visibleChallenges.get(0)).getId();
        }
        return res;
    }
}
