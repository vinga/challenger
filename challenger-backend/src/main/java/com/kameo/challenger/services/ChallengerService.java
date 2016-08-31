package com.kameo.challenger.services;

import com.challenger.eviauth.odb.ApplicationODB;
import com.challenger.eviauth.odb.PermissionODB;
import com.challenger.eviauth.odb.SessionODB;
import com.challenger.eviauth.odb.UserODB;
import com.kameo.challenger.utils.odb.AnyDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Optional;



@Transactional
@Component
public class ChallengerService {
    private Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Inject
    UserRepo userRepo;

    @Inject
    AnyDAO anyDao;

    public void loginUser(String login, String apiKey) {
        UserODB user = userRepo.getUserByLogin(login);

        ApplicationODB app = anyDao.getOnlyOne(ApplicationODB.class, a -> a.getApiKey().equals(apiKey));
        Optional<PermissionODB> first = anyDao.streamAll(PermissionODB.class)
                                              .where(p -> p.getUser().equals(user)
                                              && p.getApplication().equals(app))
                                              .findFirst();
       log.error("FOUDN "+first);
        if (first.isPresent()) {
            SessionODB session = new SessionODB();
            session.setUser(user);
            anyDao.getEm().persist(session);
        }
    }

}
