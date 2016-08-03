import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.URL;

/**
 * Created by kmyczkowska on 2016-08-03.
 */
public class Main {

    public static void main(String[] args) {
/*
        final WebAppContext bb = new WebAppContext();
        final Server server = new Server(9780);


        bb.setServer(server);
        // bb.setContextPath("/");
        // bb.setWar("C:\\projekty\\logix\\logixworkspace\\logix-web-va\\target\\logix-web-0.0.1-SNAPSHOT.war");
        // bb.setResourceBase("VAADIN");

        bb.setContextPath("/logix");
        bb.setWar("src/main/webapp");
        bb.setResourceBase("src/main/webapp");
        bb.setParentLoaderPriority(true);

        server.setHandler(bb);


        server.start();
*/



        VertxOptions ops=new VertxOptions().setHAEnabled(true);
        Vertx vertx=Vertx.vertx();


          vertx.deployVerticle(new MainVerticle());

/*
            JsonObject config = new JsonObject().put("name", "tim").put("directory", "/blah");
            DeploymentOptions options = new DeploymentOptions().setConfig(config);
            //options.setInstances(5);
            vertx.deployVerticle(ctx.getBean(RegisterVerticle.class), options, res2 -> {
                q.offer(res2);
                if (res.succeeded()) {
                    System.out.println("Deployment id is: " + res2.result());
                } else {
                    System.out.println("Deployment failed!");
                }
            });
            EventBus eventBus = vertx.eventBus();
            System.out.println("We now have a clustered event bus: " + eventBus);*/
        System.out.println("STARTED");
    }
}
