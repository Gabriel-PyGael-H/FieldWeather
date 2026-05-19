package es.ulpgc.datos.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.*;

public class HistoryLoader {
    private final Datamart datamart;
    private final String eventStorePath;

    public HistoryLoader(Datamart datamart, String eventStorePath) {
        this.datamart = datamart;
        this.eventStorePath = eventStorePath;
    }

    public void loadFootballHistory() {
        loadFromPath(Paths.get(eventStorePath, "Football"), "Football");
        loadFromPath(Paths.get(eventStorePath, "Weather"), "Weather");
    }

    private void loadFromPath(Path basePath, String topic) {
        if (!Files.exists(basePath)) return;
        try (var stream = Files.walk(basePath)) {
            stream.filter(p -> p.toString().endsWith(".events")).forEach(file -> {
                try {
                    Files.lines(file).filter(l -> !l.isBlank()).forEach(line -> {
                        JsonObject event = JsonParser.parseString(line).getAsJsonObject();
                        if (topic.equals("Football")) {
                            String home = event.get("homeTeam").getAsString();
                            datamart.insertMatchWeather(
                                    home,
                                    event.get("awayTeam").getAsString(),
                                    event.get("homeScore").getAsInt(),
                                    event.get("awayScore").getAsInt(),
                                    event.get("matchDate").getAsString(),
                                    getCityForTeam(home.trim()),
                                    null, null, "Historical data",
                                    event.get("ts").getAsString()
                            );
                        } else {
                            String city = normalize(event.get("city").getAsString());
                            double temp = event.get("temperature").getAsDouble();
                            int hum = event.get("humidity").getAsInt();
                            String desc = event.get("description").getAsString();
                            String time = event.get("predictionTime").getAsString();

                            datamart.updateWeather(city, temp, hum, desc, time);
                        }
                    });
                } catch (Exception ignored) {}
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static String normalize(String city) {
        if (city.contains("Palma")) return "Palma de Mallorca";
        if (city.contains("Seville")) return "Sevilla";
        if (city.contains("Vitoria")) return "Vitoria-Gasteiz";
        if (city.contains("Castell")) return "Castellón";
        if (city.contains("San Sebastian") || city.contains("Sebastián")) return "San Sebastian";
        return city;
    }

    public static String getCityForTeam(String team) {
        return switch (team) {
            case "Real Madrid CF", "Club Atlético de Madrid", "Getafe CF", "Rayo Vallecano de Madrid" -> "Madrid";
            case "FC Barcelona", "RCD Espanyol de Barcelona" -> "Barcelona";
            case "Sevilla FC", "Real Betis Balompié" -> "Sevilla";
            case "Valencia CF", "Levante UD" -> "Valencia";
            case "Villarreal CF" -> "Castellón";
            case "Athletic Club" -> "Bilbao";
            case "Real Sociedad de Fútbol" -> "San Sebastian";
            case "RC Celta de Vigo" -> "Vigo";
            case "CA Osasuna" -> "Pamplona";
            case "RCD Mallorca" -> "Palma de Mallorca";
            case "Girona FC" -> "Girona";
            case "Deportivo Alavés" -> "Vitoria-Gasteiz";
            case "Elche CF" -> "Elche";
            case "Real Oviedo" -> "Oviedo";
            default -> "Unknown";
        };
    }
}