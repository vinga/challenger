package com.kameo.challenger.services;


import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.jpa.JPQL;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Transactional
@Component
public class ChallengerService {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    private UserRepo userRepo;

    @Inject
    private AnyDAO anyDao;

    public List<ChallengeActionODB> getChallengeActions(long userId, long challengeContractId) {
        return anyDao.streamAll(ChallengeActionODB.class)
                     .where(ca -> ca.getChallengeContract().getId() == challengeContractId &&
                             ca.getUser().getId() == userId)
                     .collect(Collectors.toList());
    }

    public void createNewChallenge(long userId, ChallengeContractODB cb) {
        cb.setId(0);
        cb.setFirst(new UserODB(userId));
        if (cb.getSecond() == null) {
            if (cb.getSecondEmail() == null)
                throw new IllegalArgumentException("Either second user or email must be provided");
            UserODB first = anyDao.reload(cb.getFirst());
            Optional<UserODB> userByEmail = findUserByEmail(cb.getSecondEmail());
            Optional<UserODB> osecond = userByEmail;

            cb.setAcceptanceUid(UUID.randomUUID().toString());
            String actionLink = ServerConfig.getConfirmEmailInvitationPattern(cb.getAcceptanceUid());
            if (osecond.isPresent()) {
                cb.setSecond(osecond.get());


                MailService.sendHtml(new MailService.Message(cb.getSecondEmail(),
                        "Invitation",
                        "Dear " + cb.getSecond().getLogin() + ",\n" +
                                first.getLogin() + " challenged you: " + cb.getLabel() + "\n" +
                                "Click <a href='" + actionLink + "'>here</a> if you accept the challenge."));
            } else {


                MailService.sendHtml(new MailService.Message(cb.getSecondEmail(),
                        "Invitation",
                        "Dear you,\n" +
                                first.getLogin() + " challenged you: " + cb.getLabel() + "\n" +
                                "Click <a href='" + actionLink + "'>here</a> if you accept the challenge."));
            }
        }
        cb.setChallengeContractStatus(ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
        anyDao.getEm().persist(cb);

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
        else if (ca.getActionStatus()==null)
            ca.setActionStatus(ActionStatus.pending);
        ca.setCreatedByUser(new UserODB(userId));
        anyDao.getEm().persist(ca);


    }


    public List<String> findUsersWithLoginsStartingWith(String friend) {
        return anyDao.streamAll(UserODB.class).where(u ->
                JPQL.like(u.getLogin(), friend + "%")
        ).map(u -> u.getLogin()).sorted().collect(Collectors.toList());
    }

    public boolean isSecondUserCreationNeeded(String uid) {
        ChallengeContractODB ccDB = anyDao.getOnlyOne(ChallengeContractODB.class,
                cc -> cc.getAcceptanceUid().equals(uid)
                        && cc.getChallengeContractStatus() == ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
        Optional<UserODB> user = findUserByEmail(ccDB.getSecondEmail());
        return !user.isPresent();
    }

    public void acceptChallengeForExistingUser(String uid, Optional<String> loginNameIfUserNeedsToBeCreated) {
        ChallengeContractODB ccDB = anyDao.getOnlyOne(ChallengeContractODB.class,
                cc -> cc.getAcceptanceUid().equals(uid)
                        && cc.getChallengeContractStatus() == ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
        if (ccDB.getSecondEmail() == null)
            throw new IllegalArgumentException();
        Optional<UserODB> ouser = findUserByEmail(ccDB.getSecondEmail());
        if (ouser.isPresent()) {
            ccDB.setSecond(ouser.get());
        } else {
            UserODB user = createUser(loginNameIfUserNeedsToBeCreated.get(), ccDB.getSecondEmail());
            ccDB.setSecond(user);
        }
        ccDB.setChallengeContractStatus(ChallengeContractStatus.ACTIVE);
        anyDao.getEm().merge(ccDB);

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
}
