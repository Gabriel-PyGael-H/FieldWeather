package es.ulpgc.datos.businessunit.control.eventprocessors;

import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.control.HistoryLoader;

public class FootballProcessor {
    private static final String DEFAULT_DESCRIPTION = "Live match data";
    private final Datamart datamart;

    public FootballProcessor(Datamart datamart) {
        this.datamart = datamart;
    }

    public void processEvent(JsonObject event) {
        try {
            String homeTeam = event.get("homeTeam").getAsString();
            String awayTeam = event.get("awayTeam").getAsString();
            int homeScore = event.get("homeScore").getAsInt();
            int awayScore = event.get("awayScore").getAsInt();
            String matchDate = event.get("matchDate").getAsString();
            String timestamp = event.get("ts").getAsString();

            String city = HistoryLoader.getCityForTeam(homeTeam.trim());

            datamart.insertMatchWeather(
                    homeTeam, awayTeam, homeScore, awayScore,
                    matchDate, city, null, null,
                    DEFAULT_DESCRIPTION, timestamp
            );
        } catch (Exception e) {
            System.err.println("Error en FootballProcessor: " + e.getMessage());
        }
    }
}