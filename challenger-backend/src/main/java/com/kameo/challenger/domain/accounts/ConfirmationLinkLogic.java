package com.kameo.challenger.domain.accounts;

import com.google.common.base.Strings;
import com.kameo.challenger.config.ServerConfig;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkODB;
import com.kameo.challenger.domain.accounts.db.ConfirmationLinkType;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.accounts.db.UserStatus;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB;
import com.kameo.challenger.domain.challenges.db.ChallengeStatus;
import com.kameo.challenger.utils.DateUtil;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by kmyczkowska on 2016-09-02.
 */
@Component
@Transactional
public class ConfirmationLinkLogic {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private MailService mailService;

    public void confirmLinkByUid(String uid) {

        ConfirmationLinkODB cclDB = anyDao.getOnlyOne(ConfirmationLinkODB.class,
                cc -> cc.getUid().equals(uid)
        );
        switch (cclDB.getConfirmationLinkType()) {
            case CHALLENGE_CONTRACT_CONFIRMATION:
                long challengeId = cclDB.getChallengeId();

                acceptChallengeForExistingUser(cclDB.getChallengeId());
                confirmEmail(cclDB);

                break;
            case EMAIL_CONFIRMATION:
                confirmEmail(cclDB);
                break;
            default:
                throw new IllegalArgumentException();
        }
        anyDao.getEm().remove(cclDB);

    }
    public void resetPassword(String uid, String newPassword) {
        ConfirmationLinkODB cclDB = anyDao.getOnlyOne(ConfirmationLinkODB.class,
                cc -> cc.getUid().equals(uid)
        );
        if (cclDB.getSysCreationDate().before(DateUtil.addDays(new Date(),-1))) {
            throw new IllegalArgumentException();
        }

        String salt = PasswordUtil.createSalt();
        cclDB.getUser().passwordHash = PasswordUtil.getPasswordHash(newPassword, salt);
        cclDB.getUser().salt=salt;
        anyDao.getEm().persist(cclDB.getUser());
    }


    private void acceptChallengeForExistingUser(long challengeContractId) {

        ChallengeODB ccDB = anyDao.getOnlyOne(ChallengeODB.class,
                cc -> cc.getId() == challengeContractId
                        && cc.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE);

        ccDB.setChallengeStatus(ChallengeStatus.ACTIVE);
        anyDao.getEm().merge(ccDB);

    }

    /**
     * Can be invoked from both confirm challenge and confirm email
     *
     * @param cclDB
     */
    private void confirmEmail(ConfirmationLinkODB cclDB) {
        String email = cclDB.getEmail();
        if (Strings.isNullOrEmpty(email))
            throw new IllegalArgumentException("Email should be filled for " + cclDB.toString() + " of type: " + cclDB.getConfirmationLinkType().name());
        Optional<UserODB> ouser = anyDao.streamAll(UserODB.class).where(u -> u.getEmail().equals(email) && u
                .getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION).findAny();
        if (ouser.isPresent()) {
            UserODB user = ouser.get();
            if (Strings.isNullOrEmpty(cclDB.getFieldLogin()) || Strings.isNullOrEmpty(cclDB.getFieldPasswordHash())) {
                throw new IllegalArgumentException("Login and password should be filled in");
            }
            user.setLogin(cclDB.getFieldLogin());
            user.setPasswordHash(cclDB.getFieldPasswordHash());
            user.setSalt(cclDB.getFieldSalt());
            user.setUserStatus(UserStatus.ACTIVE);
            anyDao.getEm().merge(user);

        }
    }


    public boolean isConfirmationLinkRequireParams(String uid) {
        ConfirmationLinkODB cclDB = anyDao.getOnlyOne(ConfirmationLinkODB.class,
                cc -> cc.getUid().equals(uid)
        );
        if (cclDB.getConfirmationLinkType() == ConfirmationLinkType.CHALLENGE_CONTRACT_CONFIRMATION) {
            long challengeId = cclDB.getChallengeId();
            String email = cclDB.getEmail();
            ChallengeParticipantODB cpa = anyDao
                    .getOnlyOne(ChallengeParticipantODB.class, c -> c.getChallenge().getId() == challengeId && c
                            .getUser().getEmail().equals(email));
            ChallengeODB cc = anyDao
                    .get(ChallengeODB.class, cclDB.getChallengeId());
            if (cpa.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE && cc.getChallengeStatus() == ChallengeStatus.WAITING_FOR_ACCEPTANCE && cpa.getUser().getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION) {
                return true;
            }

        }
        return false;
    }

    public void fillLoginAndPasswordToConfirmationLink(String uid, String login, String password) {
        if (!isConfirmationLinkRequireParams(uid))
            throw new IllegalArgumentException("This confirmation link doesn't require params");
        ConfirmationLinkODB cclDB = anyDao.getOnlyOne(ConfirmationLinkODB.class,
                cc -> cc.getUid().equals(uid)
        );
        cclDB.setFieldLogin(login);
        cclDB.setFieldSalt(PasswordUtil.createSalt());
        cclDB.setFieldPasswordHash(PasswordUtil.getPasswordHash(password, cclDB.getFieldSalt()));
        anyDao.getEm().merge(cclDB);
    }


    public void createAndSendChallengeConfirmationLink(ChallengeODB cb, ChallengeParticipantODB cp) {
        ConfirmationLinkODB ccl = new ConfirmationLinkODB();
        ccl.setEmail(cp.getUser().getEmail());
        ccl.setChallengeId(cb.getId());
        ccl.setConfirmationLinkType(ConfirmationLinkType.CHALLENGE_CONTRACT_CONFIRMATION);
        ccl.setUid(UUID.randomUUID().toString());
        anyDao.getEm().persist(ccl);


        String login = "you";
        if (cp.getUser().getUserStatus() != UserStatus.WAITING_FOR_EMAIL_CONFIRMATION)
            login = cp.getUser().getLogin();
        mailService.sendHtml(new MailService.Message(cp.getUser().getEmail(),
                "Invitation",
                "Dear " + login + ",\n" +
                        cb.getCreatedBy().getLogin() + " challenged you: " + cb.getLabel() + "\n" +
                        "Click <a href='" + toActionLink(ccl) + "'>here</a> if you accept the challenge."));
    }

    public void createAndSendEmailConfirmationLink(String login, String password, String email) {
        ConfirmationLinkODB ccl = new ConfirmationLinkODB();
        ccl.setEmail(email);
        ccl.setFieldLogin(login);
        ccl.setFieldSalt(PasswordUtil.createSalt());
        ccl.setFieldPasswordHash(PasswordUtil.getPasswordHash(password, ccl.getFieldSalt()));
        ccl.setConfirmationLinkType(ConfirmationLinkType.EMAIL_CONFIRMATION);
        ccl.setUid(UUID.randomUUID().toString());

        mailService.sendHtml(new MailService.Message(email,
                "Email confirmation",
                "Dear you,\n" +
                        "Click <a href='" + toActionLink(ccl) + "'>here</a> to confirm you account."));
        anyDao.getEm().persist(ccl);
    }


    private String toActionLink(ConfirmationLinkODB cl) {
        return ServerConfig.getConfirmEmailInvitationPattern(cl.getUid());
    }

    public void createAndSendPasswordResetLink(UserODB u) {
        ConfirmationLinkODB ccl = new ConfirmationLinkODB();
        ccl.setEmail(u.getEmail());
        ccl.setUser(u);
        ccl.setConfirmationLinkType(ConfirmationLinkType.PASSWORD_RESET);
        ccl.setUid(UUID.randomUUID().toString());

        mailService.sendHtml(new MailService.Message(u.getEmail(),
                "Password reset",
                "Dear you,\n" +
                        "Click <a href='" + toActionLink(ccl) + "'>here</a> to reset your password."));
        anyDao.getEm().persist(ccl);
    }
}