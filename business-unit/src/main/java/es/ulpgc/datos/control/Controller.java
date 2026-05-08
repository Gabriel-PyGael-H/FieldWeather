package es.ulpgc.datos.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.ulpgc.datos.datamart.Datamart;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Controller {
    private final Datamart datamart;

    public Controller(Datamart datamart) {
        this.datamart = datamart;
    }

    public void registerRoutes(Javalin app) {
        getmatches(app);
        getMatchesByCity(app);
        getMatchesByTeam(app);
        getWeatherByCity(app);
        getRainAlerts(app);

        app.get("/recommend/{team}", this::getRecommendation);
    }

    private void getRainAlerts(Javalin app) {
        app.get("/alerts/rain", this::getRainAlerts);
    }

    private void getWeatherByCity(Javalin app) {
        app.get("/weather/{city}", this::getWeatherByCity);
    }

    private void getMatchesByTeam(Javalin app) {
        app.get("/matches/team/{team}", this::getMatchesByTeam);
    }

    private void getMatchesByCity(Javalin app) {
        app.get("/matches/{city}", this::getMatchesByCity);
    }

    private void getmatches(Javalin app) {
        app.get("/matches", this::getMatches);
    }

    private void getMatches(Context ctx) throws SQLException {
        ctx.json(resultSetToJson(datamart.queryAll()).toString());
    }

    private void getMatchesByCity(Context ctx) throws SQLException {
        String city = ctx.pathParam("city");
        ctx.json(resultSetToJson(datamart.queryByCity(city)).toString());
    }

    private void getMatchesByTeam(Context ctx) throws SQLException {
        String team = ctx.pathParam("team");
        ctx.json(resultSetToJson(datamart.queryByTeam(team)).toString());
    }

    private void getWeatherByCity(Context ctx) throws SQLException {
        String city = ctx.pathParam("city");
        ResultSet rs = datamart.queryWeatherByCity(city);
        JsonObject obj = new JsonObject();
        if (rs.next()) {
            obj.addProperty("city", rs.getString("city"));
            obj.addProperty("temperature", rs.getDouble("temperature"));
            obj.addProperty("humidity", rs.getInt("humidity"));
            obj.addProperty("description", rs.getString("description"));
        } else {
            obj.addProperty("message", "No data for " + city);
        }
        ctx.json(obj.toString());
    }

    private void getRainAlerts(Context ctx) throws SQLException {
        ctx.json(resultSetToJson(datamart.queryRainyMatches()).toString());
    }
    private void getRecommendation(Context ctx) throws SQLException {
        String team = ctx.pathParam("team");
        String now = LocalDateTime.now().toString();
        ResultSet rs = datamart.queryByTeam(team);

        boolean found = false;
        while (rs.next()) {
            String matchDate = rs.getString("match_date");
            if (matchDate != null && matchDate.compareTo(now) >= 0) {
                JsonObject res = new JsonObject();
                res.addProperty("team", team);
                res.addProperty("date", matchDate);
                res.addProperty("city", rs.getString("city"));
                double t = rs.getDouble("temperature");
                String d = rs.getString("description");
                res.addProperty("weather", t + "°C, " + d);

                String rec = "Enjoy the match!";
                if (d.toLowerCase().contains("rain")) rec = "Take an umbrella!";
                else if (t < 15) rec = "Wear a warm coat!";

                res.addProperty("recommendation", rec);
                ctx.json(res.toString());
                found = true;
                break;
            }
        }
        if (!found) ctx.status(404).result("No upcoming matches.");
    }

    private JsonArray resultSetToJson(ResultSet rs) throws SQLException {
        JsonArray result = new JsonArray();
        while (rs.next()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("homeTeam", rs.getString("home_team"));
            obj.addProperty("awayTeam", rs.getString("away_team"));
            obj.addProperty("homeScore", rs.getInt("home_score"));
            obj.addProperty("awayScore", rs.getInt("away_score"));
            obj.addProperty("matchDate", rs.getString("match_date"));
            obj.addProperty("city", rs.getString("city"));
            obj.addProperty("temperature", rs.getDouble("temperature"));
            obj.addProperty("humidity", rs.getInt("humidity"));
            obj.addProperty("description", rs.getString("description"));
            result.add(obj);
        }
        return result;
    }
}