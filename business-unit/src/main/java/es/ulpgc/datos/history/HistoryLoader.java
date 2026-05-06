package es.ulpgc.datos.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.datamart.Datamart;
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
            stream.filter(p -> p.toString().endsWith(".events"))
                    .forEach(file -> loadFile(file, topic));
        } catch (IOException e) {
            System.err.println("Error en ruta: " + e.getMessage());
        }
    }

    private void loadFile(Path file, String topic) {
        try {
            Files.lines(file).forEach(line -> {
                try {
                    if (line.isBlank()) return;
                    JsonObject event = JsonParser.parseString(line).getAsJsonObject();
                    if (topic.equals("Football")) {
                        String home = event.get("homeTeam").getAsString();
                        String away = event.get("awayTeam").getAsString();
                        String date = event.get("matchDate").getAsString();

                        int hScore = event.get("homeScore").getAsInt();
                        int aScore = event.get("awayScore").getAsInt();
                        String ts = event.get("ts").getAsString();

                        datamart.insertMatchWeather(home, away, hScore, aScore, date, getCityForTeam(home), null, null, "No weather data available yet", ts);
                    } else if (topic.equals("Weather")) {
                        datamart.updateWeather(
                                event.get("city").getAsString(),
                                event.get("temperature").getAsDouble(),
                                event.get("humidity").getAsInt(),
                                event.get("description").getAsString(),
                                event.get("predictionTime").getAsString()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Línea corrupta en " + file.getFileName() + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private String getCityForTeam(String team) {
        return switch (team) {
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
            case "Deportivo Alavés" -> "Vitoria";
            case "Elche CF" -> "Elche";
            case "Real Oviedo" -> "Oviedo";
            default -> "Unknown";
        };
    }
}