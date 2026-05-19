package es.ulpgc.datos.view;

import es.ulpgc.datos.control.Datamart;
import io.javalin.Javalin;

public class UIService {
    private final Datamart datamart;

    public UIService(Datamart datamart) {
        this.datamart = datamart;
    }

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        }).start(port);

        app.get("/recommend/{team}", ctx -> {
            var res = datamart.getRecommendation(ctx.pathParam("team"));
            if (res != null) {
                ctx.contentType("application/json");
                ctx.result(res.toString());
            } else {
                ctx.status(404).result("No hay partidos próximos.");
            }
        });

        app.get("/matches", ctx -> {
            ctx.contentType("application/json");
            ctx.result(datamart.getAllMatches().toString());
        });

        app.get("/weather/{city}", ctx -> {
            ctx.contentType("application/json");
            ctx.result(datamart.getMatchesByCity(ctx.pathParam("city")).toString());
        });

        System.out.println("Business Unit operativa en http://localhost:" + port);
    }
}