import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.jwt.impl.JWT;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import kaleidoscope.ShapesGenerator1;
import kaleidoscope.SvgGenerator;
import restapi.ChallengeActionsService;
import util.CertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;

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

        router.get("/api/newAvatar").handler((rc)-> {
            SvgGenerator gen=new SvgGenerator();
            String str=gen.toSvgString(new ShapesGenerator1());
            rc.response()
              .putHeader("content-type", "text/html; charset=utf-8")
              .putHeader("Access-Control-Allow-Origin", "*")
              .end(str);

        });
        router.get("/api/newAvatar/:id").handler((rc)-> {
            SvgGenerator gen=new SvgGenerator();
            String id = rc.request().getParam("id");
            String str=gen.toSvgString(new ShapesGenerator1(Long.parseLong(id)));
            rc.response()
              .putHeader("content-type", "text/html; charset=utf-8")
              .putHeader("Access-Control-Allow-Origin", "*")
              .end(str);

        });


        try {


            CertUtil.recreateKeystoreWithJWTKey("testSecret","keystore.jceks","HS256");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JWTAuth jwt = JWTAuth.create(vertx, new JsonObject()
                .put("keyStore", new JsonObject()
                        .put("type", "jceks")
                        .put("path", "keystore.jceks")
                        .put("password", "testSecret")));

      //  router.route("/api/*").handler(JWTAuthHandler.create(jwt, "/api/newToken"));

        ChallengeActionsService challengeActionsService = new ChallengeActionsService();
        challengeActionsService.registerRoutes(router);

        router.post("/api/newToken").handler((rc)-> {

            String login = rc.request().getParam("login");
            String pass = rc.request().getParam("pass");
            long userId=challengeActionsService.getUserIdFromLogin(login);
            String token = jwt.generateToken(new JsonObject().put("userId", userId),  new JWTOptions().setAlgorithm("HS256").setExpiresInSeconds(600L));
            System.out.println("GENERATED TOKEN "+token);
            rc.response().putHeader("Access-Control-Allow-Origin", "*").end(token);




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

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA","SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);


        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();


    }
}
