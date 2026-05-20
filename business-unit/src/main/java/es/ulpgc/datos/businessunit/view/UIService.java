package es.ulpgc.datos.businessunit.view;

import es.ulpgc.datos.businessunit.control.Datamart;
import io.javalin.Javalin;
import io.javalin.http.Context;

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

        app.get("/recommend/{team}", this::recommend);
        app.get("/matches", this::matches);
        app.get("/weather/{city}", this::weather);

        System.out.println("Business Unit operativa en http://localhost:" + port);
    }

    private void recommend(Context ctx) {
        var res = datamart.getRecommendation(ctx.pathParam("team"));
        if (res != null) {
            ctx.contentType("application/json");
            ctx.result(res.toString());
        } else {
            ctx.status(404).result("No hay partidos próximos.");
        }
    }

    private void matches(Context ctx) {
        ctx.contentType("application/json");
        ctx.result(datamart.getAllMatches().toString());
    }

    private void weather(Context ctx) {
        ctx.contentType("application/json");
        ctx.result(datamart.getMatchesByCity(ctx.pathParam("city")).toString());
    }
}