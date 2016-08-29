package restapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.vertx.core.json.Json;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import kaleidoscope.ShapesGenerator1;
import kaleidoscope.SvgGenerator;
import util.JWTHelper;

import java.util.List;

/**
 * Created by kmyczkowska on 2016-08-12.
 */
public class ChallengeActionsService {
    List<ChallengeActionDTO> challengeActionsList= Lists.newArrayList();

    {

        // {id: 1, icon: "add", actionName: "Odkurzyć", actionType: "Every Week", actionStatus: "Done"},
        // {id: 2, icon: "swap_calls", actionName: "Podlać kwiatki", actionType: "Adhoc", actionStatus: "Pending"}


        ChallengeActionDTO task1=new Gson().fromJson("{id: 1, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"Every Week\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task2=new Gson().fromJson("{id: 2, icon: \"swap_calls\", actionName: \"Podlać kwiatki\", actionType: \"adhoc\", actionStatus: \"Pending\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task3=new Gson().fromJson("{id: 3, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"weekly\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task4=new Gson().fromJson("{id: 4, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"weekly\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task5=new Gson().fromJson("{id: 5, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"Every Week\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task6=new Gson().fromJson("{id: 6, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"Every Week\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task7=new Gson().fromJson("{id: 7, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"Every Week\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        ChallengeActionDTO task8=new Gson().fromJson("{id: 8, icon: \"add\", actionName: \"Zrobić REST service\", actionType: \"Every Week\", actionStatus: \"Done\"}", ChallengeActionDTO.class);
        challengeActionsList.add(task1);
        challengeActionsList.add(task2);
        challengeActionsList.add(task3);
        challengeActionsList.add(task4);
        challengeActionsList.add(task5);
        challengeActionsList.add(task6);
        challengeActionsList.add(task7);
        challengeActionsList.add(task8);
    }
    public long getUserIdFromLogin(String login) {
        if (login.equals("kami"))
            return 1;
        else if (login.equals("jack"))
            return 2;
        throw new IllegalArgumentException();
    }

    public void registerRoutes(Router router) {
        router.route("/api/challengeActions*").handler(BodyHandler.create());
        router.get("/api/challengeActions").handler(this::getAll);


    }


    public void getAll(RoutingContext routingContext) {

        long userId = JWTHelper.getUserId(routingContext);
        System.out.println("userid "+userId);

       // UserTableDTO ut=new UserTableDTO();
        //ut.actionsList=challengeActionsList;
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .putHeader("Access-Control-Allow-Origin", "*")
                .end(Json.encodePrettily(challengeActionsList));
    }
}
