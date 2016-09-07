package com.kameo.challenger.logic;

import com.google.common.base.Strings;
import com.kameo.challenger.odb.ChallengeContractODB;
import com.kameo.challenger.odb.UserODB;
import com.kameo.challenger.odb.UserStatus;
import com.kameo.challenger.utils.PasswordUtil;
import com.kameo.challenger.utils.auth.jwt.AbstractAuthFilter;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Optional;

/**
 * Created by kmyczkowska on 2016-09-02.
 */

@Transactional
@Component
public class LoginLogic {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());
    @Inject
    private AnyDAO anyDao;
    @Inject
    private ConfirmationLinkLogic confirmationLinkLogic;

    public long login(String login, String pass) throws AbstractAuthFilter.AuthException {
        if (Strings.isNullOrEmpty(pass)) {
            throw new AbstractAuthFilter.AuthException("No password");
        }

        Optional<UserODB> user = anyDao.streamAll(UserODB.class)
                                       .where(u -> u.getLogin().equals(login))
                                       .findAny();
        if (user.isPresent()) {
            UserODB u = user.get();

            if (u.getUserStatus() == UserStatus.WAITING_FOR_EMAIL_CONFIRMATION)
                throw new AbstractAuthFilter.AuthException("Please confirm your email first");
            if (u.getPasswordHash().equals(PasswordUtil.getPasswordHash(pass, u.getSalt()))) {
                if (u.getUserStatus() == UserStatus.SUSPENDED && new DateTime(u.getSuspendedDueDate()).isBeforeNow()) {
                    // lest unblock it, but how we can know which status was previous?
                    u.setUserStatus(UserStatus.ACTIVE);
                    anyDao.getEm().merge(u);
                }
                if (u.getUserStatus() == UserStatus.SUSPENDED) {
                    throw new AbstractAuthFilter.AuthException("There have been several failed attempts to sign in from this account or IP address. Please wait a while and try again later.");
                } else if (u.getUserStatus() != UserStatus.ACTIVE) {
                    throw new AbstractAuthFilter.AuthException("Your account is not active");
                } else
                    return u.getId();

            } else {
                u.setFailedLoginsNumber(u.getFailedLoginsNumber() + 1);

                if (u.getFailedLoginsNumber() > 10) {
                    u.setUserStatus(UserStatus.SUSPENDED);
                    u.setSuspendedDueDate(DateUtils.addMinutes(new Date(), 20));
                }
                anyDao.getEm().merge(u);
                throw new AbstractAuthFilter.AuthException("Wrong credentials");
            }
        } else {
            throw new AbstractAuthFilter.AuthException("User with login '" + login + "' doesn't exist");
        }
    }

    public UserODB createPendingUserWithEmailOnly(ChallengeContractODB cb) {
        UserODB user = new UserODB();
        user.setLogin("");
        user.setEmail(cb.getSecond().getEmail());
        user.setUserStatus(UserStatus.WAITING_FOR_EMAIL_CONFIRMATION);
        anyDao.getEm().persist(user);
        return user;
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