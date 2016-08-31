package challenger.web.rest.impl;

import com.challenger.eviauth.odb.UserODB;
import com.challenger.eviauth.services.UserRepo;
import com.challenger.eviauth.utils.ReflectionUtils;
import com.challenger.eviauth.web.rest.api.ITestService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
@Component
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestService implements ITestService {

    @Inject
    private UserRepo userRepo;

    @GET
    @Path("hello")
    public String hellp() {
        System.out.println(userRepo.getAll());
        return "Hello there";
    }


    @GET
    @Path("getUsers")
    public List<User> getUsers() {
        System.out.println(userRepo.getAll());
        return ReflectionUtils.copyList(userRepo.getAll(), User.class);
    }

    @GET
    @Path("getUser/{login}")
    public User getUserByLogin(@PathParam("login") String login) {
        System.out.println("login " + login);
        UserODB userOdb = userRepo.getUserByLogin(login);
        return ReflectionUtils.copy(userOdb, User.class);
    }


}