package es.ulpgc.datos.businessunit.control.eventprocessors;

import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.control.HistoryLoader;

public class FootballProcessor {
    private final Datamart datamart;

    public FootballProcessor(Datamart datamart) {
        this.datamart = datamart;
    }

    public void processEvent(JsonObject event) {
        try {
            String home = event.get("homeTeam").getAsString();
            datamart.insertMatchWeather(
                    home,
                    event.get("awayTeam").getAsString(),
                    event.get("homeScore").getAsInt(),
                    event.get("awayScore").getAsInt(),
                    event.get("matchDate").getAsString(),
                    HistoryLoader.getCityForTeam(home.trim()),
                    null, null, "Live match data",
                    event.get("ts").getAsString()
            );
        } catch (Exception e) {
            System.err.println("Error en FootballProcessor: " + e.getMessage());
        }
    }
}