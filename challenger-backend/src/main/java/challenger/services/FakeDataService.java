package challenger.services;

import com.challenger.eviauth.odb.ApplicationODB;
import com.challenger.eviauth.odb.PermissionODB;
import com.challenger.eviauth.odb.SubscriptionODB;
import com.challenger.eviauth.odb.UserODB;
import com.challenger.eviauth.utils.odb.AnyDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.UUID;


@Component
@Transactional
public class FakeDataService implements CommandLineRunner {

    @Inject
    private AnyDAO anyDao;

    public static class Data {

    }

    public static Data data=new Data();
    @Override
    public void run(String... strings) throws Exception {

        if (anyDao.streamAll(UserODB.class).findFirst().isPresent())
            return;



        createUsers();


    }

    private void createUsers() {
        UserODB u=new UserODB();
        u.setLogin("johny");
        u.setFirstName("John");
        u.setLastName("Snow");
        anyDao.getEm().persist(u);
        data.userJohny=u;
        System.out.println("Persisted "+u);

        u=new UserODB();
        u.setLogin("alice");
        u.setFirstName("Alice");
        u.setLastName("Alice");
        data.userAlice=u;
        anyDao.getEm().persist(u);




    }


}
