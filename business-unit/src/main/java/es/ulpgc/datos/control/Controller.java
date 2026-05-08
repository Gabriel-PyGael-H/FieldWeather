package es.ulpgc.datos.control;

import com.google.gson.JsonObject;
import es.ulpgc.datos.datamart.Datamart;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class Controller {
    private final Datamart datamart;

    public Controller(Datamart datamart) {
        this.datamart = datamart;
    }

    public void registerRoutes(Javalin app) {
        app.get("/matches", ctx -> ctx.json(datamart.getAllMatches().toString()));

        app.get("/matches/{city}", ctx -> {
            ctx.json(datamart.getMatchesByCity(ctx.pathParam("city")).toString());
        });

        app.get("/recommend/{team}", this::getRecommendation);
    }

    private void getRecommendation(Context ctx) {
        String team = ctx.pathParam("team");
        JsonObject rec = datamart.getRecommendation(team);

        if (rec != null) ctx.json(rec.toString());
        else ctx.status(404).result("No hay partidos próximos.");
    }
}