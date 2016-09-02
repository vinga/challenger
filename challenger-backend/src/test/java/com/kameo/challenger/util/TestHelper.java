package com.kameo.challenger.util;


import com.google.common.collect.Lists;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.MailService;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.jinq.orm.stream.JinqStream;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class TestHelper {
    public static final String QUOTED_STR = "\"([^\"]*)\"";
    public static final String DIGIT = "(\\d+)";
    private final List<MailService.Message> messages= Lists.newArrayList();

    @Inject
    private AnyDAO anyDao;

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
                c -> c.getChallengeContractStatus()== ChallengeContractStatus.ACTIVE
        );

    }

    @Transactional
    public void clearSchema() {
        anyDao.getEm().createNativeQuery("TRUNCATE SCHEMA public AND COMMIT").executeUpdate();
        anyDao.getEm().clear();
        getSentMessagesList().clear();
    }

    @Transactional
    public void acceptAllChallengeActions(long id, List<ChallengeActionODB> collect) {
        for (ChallengeActionODB c: collect) {
            c.setActionStatus(ActionStatus.pending);
            anyDao.getEm().merge(c);
        }
    }

    public List<MailService.Message> getSentMessagesList() {
        return messages;
    }
}
