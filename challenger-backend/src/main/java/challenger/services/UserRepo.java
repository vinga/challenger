package challenger.services;

import com.challenger.eviauth.odb.UserODB;
import com.challenger.eviauth.utils.odb.AnyDAO;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Repository
public class UserRepo {

    @Inject
    private AnyDAO anyDao;

    public List<UserODB> getAll() {
        List<UserODB> users = anyDao.getAll(UserODB.class);
        return users;
    }

    public UserODB getUserByLogin(String login) {
        return anyDao.streamAll(UserODB.class)
                     .where(u -> u.getLogin().equals(login))
                     .findOne().get();
    }



}
