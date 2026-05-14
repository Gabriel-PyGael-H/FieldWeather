package es.ulpgc.datos.control;

import es.ulpgc.datos.model.Datamart;
import io.javalin.Javalin;

public class Controller {
    private final Datamart datamart;

    public Controller(Datamart datamart) { this.datamart = datamart; }

    public void registerRoutes(Javalin app) {
        app.get("/recommend/{team}", ctx -> {
            var res = datamart.getRecommendation(ctx.pathParam("team"));
            if (res != null) ctx.json(res.toString());
            else ctx.status(404).result("No hay partidos próximos.");
        });

        app.get("/matches", ctx -> ctx.json(datamart.getAllMatches().toString()));
        app.get("/weather/{city}", ctx -> ctx.json(datamart.getMatchesByCity(ctx.pathParam("city")).toString()));
    }
}