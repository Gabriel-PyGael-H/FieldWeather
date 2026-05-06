package es.ulpgc.datos.footballfeeder.model;

import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MatchMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public Match map(JsonObject m) {
        String homeTeam = m.getAsJsonObject("homeTeam").get("name").getAsString();
        String awayTeam = m.getAsJsonObject("awayTeam").get("name").getAsString();

        JsonObject fullTime = m.getAsJsonObject("score").getAsJsonObject("fullTime");
        int homeScore = fullTime.get("home").isJsonNull() ? 0 : fullTime.get("home").getAsInt();
        int awayScore = fullTime.get("away").isJsonNull() ? 0 : fullTime.get("away").getAsInt();

        String status = m.get("status").getAsString();
        String competition = m.getAsJsonObject("competition").get("name").getAsString();
        LocalDateTime matchDate = LocalDateTime.parse(m.get("utcDate").getAsString(), FORMATTER);

        // Lógica de ciudad integrada
        String city = switch (homeTeam) {
            case "Real Madrid CF", "Club Atlético de Madrid", "Getafe CF", "Rayo Vallecano de Madrid" -> "Madrid";
            case "FC Barcelona", "RCD Espanyol de Barcelona" -> "Barcelona";
            case "Sevilla FC", "Real Betis Balompié" -> "Sevilla";
            case "Valencia CF", "Levante UD" -> "Valencia";
            case "Athletic Club" -> "Bilbao";
            case "Girona FC" -> "Girona";
            case "CA Osasuna" -> "Pamplona";
            case "RCD Mallorca" -> "Palma de Mallorca";
            case "Real Sociedad de Fútbol" -> "San Sebastian";
            case "Villarreal CF" -> "Villarreal";
            case "RC Celta de Vigo" -> "Vigo";
            case "Deportivo Alavés" -> "Vitoria-Gasteiz";
            case "Elche CF" -> "Elche";
            case "Real Oviedo" -> "Oviedo";
            default -> "Unknown";
        };

        return new Match(homeTeam, awayTeam, homeScore, awayScore, status, competition, matchDate, city);
    }
}