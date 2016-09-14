package com.kameo.challenger.logic;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.odb.AnyDAO;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.jpa.JPQL;
import org.jinq.orm.stream.JinqStream;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.*;
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


    public long getOtherChallengeUser(long callerId, long challengeId) {
        ChallengeODB cc = anyDao.get(ChallengeODB.class, challengeId);
        if (cc.getFirst().getId()==callerId)
            return cc.getSecond().getId();
        else if (cc.getSecond().getId()==callerId)
            return cc.getFirst().getId();
        else throw new IllegalArgumentException();

    }
    public List<TaskODB> getTasksAssignedToOther(long callerId, long challengeId, Date date) {
        long userId=getOtherChallengeUser(callerId,challengeId);
        return getTasksAssignedToPerson(callerId,userId,challengeId, date);
    }

    public List<TaskODB> getTasksAssignedToPerson(long callerId, long userId, long challengeId, Date date) {




        JinqStream.Where<TaskODB, ?> permissionToViewContract = ca -> ca.getChallenge().getFirst()
                                                                        .getId() == callerId || ca.getChallenge()
                                                                                                  .getSecond()
                                                                                                  .getId() == callerId;
        if (callerId==userId)
            updateSeendDateOfChallegeContract(callerId, challengeId);
        List<TaskODB> res = anyDao.streamAll(TaskODB.class)
                                  .where(permissionToViewContract)
                                  .where(ca -> ca.getChallenge().getId() == challengeId &&
                                          ca.getUser().getId() == userId)
                                  .filter(ca ->
                                          ca.getTaskType() != TaskType.onetime ||
                                                  ca.getTaskType() == TaskType.onetime &&
                                                          new DateTime(ca.getDueDate()).isAfter(date.getTime()))
                                  .collect(Collectors.toList());

        getTaskProgress(res, date).stream().forEach(
                tp -> res.stream().filter(task -> task.getId() == tp.getTask().getId()).findAny().get().setDone(tp.isDone())
        );

        return res;

    }


    private void updateSeendDateOfChallegeContract(long callerId, long challengeContractId) {
        ChallengeODB cc = anyDao.get(ChallengeODB.class, challengeContractId);
        if (cc.getFirst().getId() != callerId && cc.getSecond().getId() != callerId)
            throw new IllegalArgumentException();
        if (cc.getFirst().getId() == callerId)
            cc.setLastSeenByFirst(new Date());
        else cc.setLastSeenBySecond(new Date());
        anyDao.getEm().merge(cc);
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
        cb.setFirst(new UserODB(userId));
        boolean confirmationByEmail = false;
        if (cb.getSecond().isNew()) {
            // lets check if such user exists
            if (cb.getSecond().getEmail() == null)
                throw new IllegalArgumentException("Either second user id or second  user email must be provided");
            Optional<UserODB> osecond = findUserByEmail(cb.getSecond().getEmail());
            confirmationByEmail = true;
            if (osecond.isPresent()) {
                cb.setSecond(osecond.get());
            } else {
                UserODB user = loginLogic.createPendingUserWithEmailOnly(cb);
                cb.setSecond(user);
            }
        }
        if (Strings.isNullOrEmpty(cb.getLabel())) {
            String first = cb.getFirst().getLogin();
            String second = cb.getSecond().getLogin();

            if (Strings.isNullOrEmpty(second))
                second = cb.getSecond().getEmail();
            cb.setLabel((first + " vs " + second).toLowerCase());
        }
        cb.setChallengeStatus(ChallengeStatus.WAITING_FOR_ACCEPTANCE);
        anyDao.getEm().persist(cb);
        if (confirmationByEmail) {
            confirmationLinkLogic.createAndSendChallengeConfirmationLink(cb);
        }
    }


    public List<ChallengeODB> getPendingChallenges(long userId) {
        return anyDao.streamAll(ChallengeODB.class)
                     .where(cc -> cc.getSecond().getId() == userId &&
                             cc.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                     .collect(Collectors.toList());
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
        JinqStream.Where<ChallengeODB, ?> permissionToViewContract = ca -> ca.getFirst().getId() == callerId || ca
                .getSecond().getId() == callerId;
        ChallengeODB challenge = anyDao.streamAll(ChallengeODB.class).where(ch -> ch.getId() == challengeId)
                                       .where(permissionToViewContract).findOne().get();
        if (challenge.getFirst().getId() == callerId)
            challenge.setLastSeenByFirst(new Date());
        else challenge.setLastSeenBySecond(new Date());
        anyDao.getEm().merge(challenge);
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
            if (cc.getFirst().getId() != callerId && cc.getSecond().getId() != callerId)
                throw new IllegalArgumentException();
            if (taskODB.getUser().getId() != cc.getFirst().getId() && taskODB.getUser().getId() != cc.getSecond()
                                                                                                     .getId())
                throw new IllegalArgumentException();

            taskODB.setTaskStatus(TaskStatus.waiting_for_acceptance);
            if (taskODB.getUser().getId() == callerId)
                taskODB.setTaskStatus(TaskStatus.accepted);


            taskODB.setCreatedByUser(new UserODB(callerId));
            anyDao.getEm().persist(taskODB);
            return taskODB;
        }
    }

    public TaskProgressODB markTaskDone(long callerId, long taskId, Date day, boolean done) {
        Date dayMidnight = DateUtil.getMidnight(day);
        TaskODB task = anyDao.get(TaskODB.class, taskId);
        if (task.getUser().getId() != callerId)
            throw new IllegalArgumentException();
        System.out.println("MARK DONE "+done);
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


    public static class ChallengeInfoDTO {
        @Getter
        Long defaultChallengeId;
        @Getter
        List<ChallengeODB> visibleChallenges = Lists.newArrayList();
    }

    public ChallengeInfoDTO getVisibleChallenges(long callerId) {
        ChallengeInfoDTO res = new ChallengeInfoDTO();
        res.visibleChallenges = anyDao.streamAll(ChallengeODB.class)
                                      .where(c -> c.getFirst().getId() == callerId || c.getSecond()
                                                                                       .getId() == callerId)
                                      .where(c -> c
                                              .getChallengeStatus() == ChallengeStatus.ACTIVE ||
                                              c.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE)
                                      .collect(Collectors.toList());
        UserODB user = anyDao.get(UserODB.class, callerId);
        res.defaultChallengeId = user.getDefaultChallengeContract();

        if (res.defaultChallengeId == null && !res.visibleChallenges.isEmpty())
            res.defaultChallengeId = res.visibleChallenges.stream()
                                                          .sorted(new Comparator<ChallengeODB>() {

                                                              @Override
                                                              public int compare(ChallengeODB o1, ChallengeODB o2) {
                                                                  Date d1;
                                                                  Date d2;
                                                                  if (o1.getFirst().getId() == callerId) {
                                                                      d1 = o1.getLastSeenByFirst();
                                                                  } else
                                                                      d1 = o1.getLastSeenBySecond();

                                                                  if (o2.getFirst().getId() == callerId) {
                                                                      d2 = o2.getLastSeenByFirst();
                                                                  } else
                                                                      d2 = o2.getLastSeenBySecond();

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
        return res;
    }
}
