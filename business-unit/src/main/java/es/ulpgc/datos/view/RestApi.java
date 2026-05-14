package es.ulpgc.datos.view;

import es.ulpgc.datos.control.Controller;
import es.ulpgc.datos.model.Datamart;
import io.javalin.Javalin;

public class RestApi {
    private final Datamart datamart;

    public RestApi(Datamart datamart) {
        this.datamart = datamart;
    }

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(port);

        Controller controller = new Controller(datamart);
        controller.registerRoutes(app);

        System.out.println("Business Unit operativa en http://localhost:" + port);
    }
}