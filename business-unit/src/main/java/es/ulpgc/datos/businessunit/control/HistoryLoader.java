package es.ulpgc.datos.businessunit.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.businessunit.control.eventprocessors.FootballProcessor;
import es.ulpgc.datos.businessunit.control.eventprocessors.WeatherProcessor;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class HistoryLoader {
    private final FootballProcessor footballProcessor;
    private final WeatherProcessor weatherProcessor;
    private final String eventStorePath;

    public HistoryLoader(FootballProcessor fp, WeatherProcessor wp, String eventStorePath) {
        this.footballProcessor = fp;
        this.weatherProcessor = wp;
        this.eventStorePath = eventStorePath;
    }

    public void loadFootballHistory() {
        loadFromPath(Paths.get(eventStorePath, "Football"), "Football");
        loadFromPath(Paths.get(eventStorePath, "Weather"), "Weather");
    }

    private void loadFromPath(Path basePath, String topic) {
        if (!Files.exists(basePath)) return;

        try {
            processPathStreams(basePath, topic);
        } catch (IOException e) {
            System.err.println("Error walking path " + basePath + ": " + e.getMessage());
        }
    }

    private void processPathStreams(Path basePath, String topic) throws IOException {
        try (Stream<Path> stream = Files.walk(basePath)) {
            stream.filter(p -> p.toString().endsWith(".events"))
                    .forEach(file -> parseEventFile(file, topic));
        }
    }

    private void parseEventFile(Path file, String topic) {
        try {
            readFileLines(file, topic);
        } catch (IOException e) {
            System.err.println("Error reading event file " + file + ": " + e.getMessage());
        }
    }
    private void readFileLines(Path file, String topic) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
            lines.filter(line -> !line.isBlank())
                    .forEach(line -> processEventLine(line, topic));
        }
    }

    private void processEventLine(String line, String topic) {
        JsonObject event = JsonParser.parseString(line).getAsJsonObject();
        routeEvent(event, topic);
    }

    private void routeEvent(JsonObject event, String topic) {
        if (topic.equals("Football")) {
            footballProcessor.processEvent(event);
        } else if (topic.equals("Weather")) {
            weatherProcessor.processEvent(event);
        }
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