import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by kmyczkowska on 2016-08-03.
 */
public class MainVerticle extends AbstractVerticle {



 /*   public RegisterVerticle(ApplicationContext ctx) {
        userDao=ctx.getBean(UserDAO.class);
    }*/

    @Override
    public void start(Future<Void> fut) {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/api/register").blockingHandler(this::register);
        router.route("/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("Register verticle");
        });
        try {
            System.out.println(new File("").getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        router.route().handler(StaticHandler.create("challenger-web/src/main/resources/webroot/static").setCachingEnabled(false));

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 9080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    private void register(RoutingContext routingContext) {
     /*   System.out.println("str "+routingContext.getBodyAsString());
        final RegisterDTO register = Json.decodeValue(routingContext.getBodyAsString(),
                RegisterDTO.class);
        UserODB user = userDao.registerUser(register);
        System.out.println("REGISTERED!!");
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(user));*/
        routingContext.next();

    }

}
