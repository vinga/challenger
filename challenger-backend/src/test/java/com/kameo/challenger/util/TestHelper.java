package com.kameo.challenger.util;


import com.google.common.collect.Lists;
import com.kameo.challenger.logic.FakeDataLogic;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinq.orm.stream.JinqStream;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * cucumber matchers:
 * quoted string  \"([^\"]*)\"
 * digit (\d+)
 */
public class TestHelper {
    public static Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());
    private final List<MailService.Message> messages = Lists.newArrayList();

    @Inject
    private AnyDAO anyDao;
    @Inject
    private FakeDataLogic fakeDataLogic;


    public List<UserODB> createUsers(String... logins) {
        return fakeDataLogic.createUsers(logins);
    }

    public ChallengeContractODB createAcceptedChallenge(Iterator<UserODB> users) {
        return fakeDataLogic.createAcceptedChallenge(users);
    }

    public UserODB myFriend() {
        return anyDao.streamAll(UserODB.class).where(u -> u.getLogin().equals("myFriend")).getOnlyValue();
    }

    public UserODB myself() {
        return anyDao.streamAll(UserODB.class).where(u -> u.getLogin().equals("myself")).getOnlyValue();
    }

    public Optional<UserODB> myselfOptional() {
        return anyDao.streamAll(UserODB.class).where(u -> u.getLogin().equals("myself")).findAny();
    }

    public ChallengeContractODB getActiveContactBetween(UserODB u1, UserODB u2) {
        JinqStream.Where<ChallengeContractODB, ?> goodUser = c ->
                (c.getFirst().equals(u1) && c.getSecond().equals(u2)) ||
                        (c.getFirst().equals(u2) && c.getSecond().equals(u1));

        return anyDao.getOnlyOne(ChallengeContractODB.class,
                goodUser,
                c -> c.getChallengeContractStatus() == ChallengeContractStatus.ACTIVE
        );

    }

    @Transactional
    public void clearSchema() {
        anyDao.getEm().createNativeQuery("TRUNCATE SCHEMA public AND COMMIT").executeUpdate();
        anyDao.getEm().clear();
        getSentMessagesList().clear();
    }

    @Transactional
    public void acceptAllChallengeActions(long userId, List<ChallengeActionODB> collect) {
        for (ChallengeActionODB c : collect) {
            if (c.getUser().getId() != userId)
                throw new IllegalArgumentException();
            c.setActionStatus(ActionStatus.pending);
            anyDao.getEm().merge(c);
        }
    }

    public List<MailService.Message> getSentMessagesList() {
        return messages;
    }
}
