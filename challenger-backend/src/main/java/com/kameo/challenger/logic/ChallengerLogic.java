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
    private UserRepo userRepo;

    @Inject
    private AnyDAO anyDao;



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
            UserODB first = anyDao.reload(cb.getFirst());
            Optional<UserODB> userByEmail = findUserByEmail(cb.getSecond().getEmail());
            Optional<UserODB> osecond = userByEmail;


            confirmationByEmail = true;

            if (osecond.isPresent()) {
                cb.setSecond(osecond.get());
            } else {
                UserODB user = new UserODB();
                user.setLogin("");
                user.setEmail(cb.getSecond().getEmail());
                user.setUserStatus(UserStatus.WAITING_FOR_EMAIL_CONFIRMATION);
                anyDao.getEm().persist(user);
                cb.setSecond(user);
            }
        }
        cb.setChallengeContractStatus(ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
        anyDao.getEm().persist(cb);

        if (confirmationByEmail) {
            ConfirmationLinkODB ccl = new ConfirmationLinkODB();
            ccl.setEmail(cb.getSecond().getEmail());
            ccl.setChallengeContractId(cb.getId());
            ccl.setConfirmationLinkType(ConfirmationLinkType.CHALLENGE_CONTRACT_CONFIRMATION);
            ccl.setUid(UUID.randomUUID().toString());
            anyDao.getEm().persist(ccl);

            confirmationLinkLogic.createAndSendChallengeConfirmationLink(cb, ccl);
        }
    }



    private Optional<UserODB> findUserByEmail(String email) {
        return anyDao.getOne(UserODB.class, u -> u.getEmail().equals(email));
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




    private UserODB createUser(String loginName, String email) {
        UserODB user = new UserODB();
        user.setLogin(loginName);
        user.setEmail(email);
        anyDao.getEm().persist(user);
        return user;
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


    public long login(String login, String pass) throws AuthException {
        if (Strings.isNullOrEmpty(pass)) {
            throw new AuthException("No password");
        }

        Optional<UserODB> user = anyDao.streamAll(UserODB.class)
                                       .where(u -> u.getLogin().equals(login))
                                       .findAny();
        if (user.isPresent()) {
            UserODB u = user.get();

            if (u.getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION)
                throw new AuthException("Please confirm your email first");
            if (u.getPasswordHash().equals(PasswordUtil.getPasswordHash(pass, u.getSalt()))) {
                if (u.getUserStatus() == UserStatus.SUSPENDED && new DateTime(u.getSuspendedDueDate()).isBeforeNow()) {
                    // lest unblock it, but how we can know which status was previous?
                    u.setUserStatus(UserStatus.ACTIVE);
                    anyDao.getEm().merge(u);
                }
                if (u.getUserStatus() == UserStatus.SUSPENDED) {
                    throw new AuthException("Your account is suspended till " + u.getSuspendedDueDate());
                } else if (u.getUserStatus() != UserStatus.ACTIVE) {
                    throw new AuthException("Your account is not active");
                } else
                    return u.getId();

            } else {
                u.setFailedLoginsNumber(u.getFailedLoginsNumber() + 1);

                if (u.getFailedLoginsNumber() > 10) {
                    u.setUserStatus(UserStatus.SUSPENDED);
                    u.setSuspendedDueDate(DateUtils.addMinutes(new Date(), 20));
                }
                anyDao.getEm().merge(u);
                throw new AuthException("Wrong credentials");
            }
        } else {
            throw new AuthException("User with login '" + login + "' doesn't exist");
        }
    }

    public boolean registerUser(String login, String password, String email) {
        Optional<ChallengeContractODB> cco = anyDao.streamAll(ChallengeContractODB.class)
                                                   .where(cc -> cc.getFirst().getEmail().equals(email) || cc
                                                           .getSecond().getEmail().equals(email)).findAny();

        if (!cco.isPresent()) {
            UserODB user = new UserODB();
            user.setLogin(login);
            user.setEmail(email);
            user.setUserStatus(UserStatus.ACTIVE);
            user.setSalt(PasswordUtil.createSalt());
            user.setPasswordHash(PasswordUtil.getPasswordHash(password, user.getSalt()));
            anyDao.getEm().persist(user);
            return true;
        } else {

            ChallengeContractODB cc = cco.get();
            boolean firsst = cc.getFirst().getEmail().equals(email) && cc.getFirst()
                                                                         .getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION;
            boolean sec = cc.getSecond().getEmail().equals(email) && cc.getSecond()
                                                                       .getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION;
            if (firsst || sec) {
                confirmationLinkLogic.createAndSendEmailConfirmationLink(login, password, email);
                return true;
            }

            return false;
        }
    }




}
