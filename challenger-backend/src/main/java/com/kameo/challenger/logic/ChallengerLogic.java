package com.kameo.challenger.logic;


import com.google.common.base.Strings;
import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.PasswordUtil;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter.AuthException;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.jpa.JPQL;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public List<ChallengeActionODB> getChallengeActions(long userId, long challengeContractId) {
        return anyDao.streamAll(ChallengeActionODB.class)
                     .where(ca -> ca.getChallengeContract().getId() == challengeContractId &&
                             ca.getUser().getId() == userId)
                     .collect(Collectors.toList());
    }

    public void createNewChallenge(long userId, ChallengeContractODB cb) {
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
        cb.setChallengeContractStatus(ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
        anyDao.getEm().persist(cb);
        if (confirmationByEmail) {
            confirmationLinkLogic.createAndSendChallengeConfirmationLink(cb);
        }
    }



    public List<ChallengeContractODB> getPendingChallenges(long userId) {
        return anyDao.streamAll(ChallengeContractODB.class)
                     .where(cc -> cc.getSecond().getId() == userId &&
                             cc.getChallengeContractStatus() == ChallengeContractStatus.WAITING_FOR_ACCEPTANCE)
                     .collect(Collectors.toList());
    }

    public void createNewChallengeAction(long userId, ChallengeActionODB ca) {
        ChallengeContractODB cc = ca.getChallengeContract();
        ChallengeContractODB ccDB = anyDao.reload(cc);
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


    public List<String> findUsersWithLoginsStartingWith(String friend) {
        return anyDao.streamAll(UserODB.class).where(u ->
                JPQL.like(u.getLogin(), friend + "%")
        ).map(u -> u.getLogin()).sorted().collect(Collectors.toList());
    }

    private Optional<UserODB> findUserByEmail(String email) {
        return anyDao.getOne(UserODB.class, u -> u.getEmail().equals(email));
    }


    public List<ChallengeActionODB> getPendingChallengeActionsForConctract(long userId, long contractId) {
        return anyDao.streamAll(ChallengeActionODB.class)
                     .where(ca -> ca.getChallengeContract().getId() == contractId &&
                             ca.getUser().getId() == userId &&
                             ca.getActionStatus() == ActionStatus.waiting_for_acceptance &&
                             ca.getChallengeContract().getChallengeContractStatus() == ChallengeContractStatus.ACTIVE)
                     .filter(ca ->
                             ca.getActionType() != ActionType.onetime ||
                                     ca.getActionType() == ActionType.onetime &&
                                             new DateTime(ca.getDueDate()).isAfterNow())
                     .collect(Collectors.toList());
    }

    public List<ChallengeActionODB> getMyDailyTodoList(long userId, long contractId) {
        return anyDao.streamAll(ChallengeActionODB.class)
                     .where(ca -> ca.getChallengeContract().getId() == contractId &&
                             ca.getUser().getId() == userId &&
                             ca.getActionStatus() != ActionStatus.waiting_for_acceptance &&
                             ca.getChallengeContract().getChallengeContractStatus() == ChallengeContractStatus.ACTIVE)
                     .filter(ca ->
                             ca.getActionType() != ActionType.onetime ||
                                     ca.getActionType() == ActionType.onetime &&
                                             new DateTime(ca.getDueDate()).isAfterNow())
                     .collect(Collectors.toList());
    }




}
