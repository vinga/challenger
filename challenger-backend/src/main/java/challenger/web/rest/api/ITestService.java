package challenger.web.rest.api;

import lombok.Data;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
public interface ITestService {


    public static @Data
    class User {
        long id;
        String firstName;
        String lastName;
    }
}
