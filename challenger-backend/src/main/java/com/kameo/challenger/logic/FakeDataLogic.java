package com.kameo.challenger.logic;


import com.google.common.collect.Lists;
import com.kameo.challenger.odb.*;
import com.kameo.challenger.utils.PasswordUtil;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;


@Component
@Transactional
public class FakeDataLogic implements CommandLineRunner {

    public static Data data = new Data();
    @Inject
    private AnyDAO anyDao;

    @Override
    public void run(String... strings) throws Exception {
        if (anyDao.streamAll(UserODB.class).findFirst().isPresent())
            return;


        createUsers();


        EntityManager em = anyDao.getEm();

        ChallengeContractODB contract1 = new ChallengeContractODB();
        contract1.setFirst(data.userKami);
        contract1.setSecond(data.userJack);
        em.persist(contract1);

        ChallengeActionODB ac1 = new ChallengeActionODB();
        ac1.setUser(data.userJack);
        ac1.setCreatedByUser(data.userKami);
        ac1.setChallengeContract(contract1);
        ac1.setActionName("Test action");
        ac1.setActionStatus(ActionStatus.pending);
        ac1.setDifficulty(1);
        ac1.setIcon("fa-car");
        ac1.setActionType(ActionType.daily);
        em.persist(ac1);

    }

    private void createUsers() {
        UserODB u = new UserODB();
        u.setLogin("jack");
        u.setEmail("jack@email.em");
        u.setSalt(PasswordUtil.createSalt());
        u.setPasswordHash(PasswordUtil.getPasswordHash("jackpass",u.getSalt()));
        anyDao.getEm().persist(u);
        data.userJack = u;

        u = new UserODB();
        u.setLogin("kami");
        u.setEmail("kami@email.em");
        u.setSalt(PasswordUtil.createSalt());
        u.setPasswordHash(PasswordUtil.getPasswordHash("kamipass",u.getSalt()));
        data.userKami = u;
        anyDao.getEm().persist(u);

        u = new UserODB();
        u.setLogin("milena");
        u.setEmail("milena@email.em");
        u.setSalt(PasswordUtil.createSalt());
        u.setPasswordHash(PasswordUtil.getPasswordHash("milenapass",u.getSalt()));
        data.userMilena = u;
        anyDao.getEm().persist(u);
    }

    public void clearSchema() {
        anyDao.getEm().createNativeQuery("TRUNCATE SCHEMA public AND COMMIT").executeUpdate();
        anyDao.getEm().clear();
    }


    public List<UserODB> createUsers(String... logins) {
        List<UserODB> res = Lists.newArrayList();
        for (String login : logins) {
            UserODB u = new UserODB();
            u.setLogin(login);
            u.setEmail(login + "@email.em");
            u.setSalt(PasswordUtil.createSalt());
            u.setPasswordHash(PasswordUtil.getPasswordHash(login+"pass",u.getSalt()));
            anyDao.getEm().persist(u);
            res.add(u);
        }
        return res;
    }

    public void createUsersWithChallenge(String u1, String u2) {
        Iterator<UserODB> users = createUsers(u1, u2).iterator();
        createPendingChallenge(users);
    }

    public ChallengeContractODB createPendingChallenge(Iterator<UserODB> users) {
        return createChallenge(users, ChallengeContractStatus.WAITING_FOR_ACCEPTANCE);
    }

    public ChallengeContractODB createAcceptedChallenge(Iterator<UserODB> users) {
        return createChallenge(users, ChallengeContractStatus.ACTIVE);
    }

    public ChallengeContractODB createChallenge(Iterator<UserODB> users, ChallengeContractStatus status) {
        ChallengeContractODB contract1 = new ChallengeContractODB();
        contract1.setFirst(users.next());
        contract1.setSecond(users.next());
        contract1.setChallengeContractStatus(status);
        anyDao.getEm().persist(contract1);
        return contract1;
    }

    public static class Data {
        UserODB userJack, userKami, userMilena;
    }


}
