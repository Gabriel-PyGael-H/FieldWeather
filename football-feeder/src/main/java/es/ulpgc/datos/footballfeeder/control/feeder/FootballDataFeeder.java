package es.ulpgc.datos.footballfeeder.control.feeder;

import com.google.gson.*;
import es.ulpgc.datos.footballfeeder.model.Match;
import es.ulpgc.datos.footballfeeder.model.MatchMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FootballDataFeeder implements FootballFeeder {

    private final String url;
    private final String apiKey;
    private final MatchMapper mapper = new MatchMapper();
    private final HttpClient client;

    public FootballDataFeeder(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public List<Match> fetchMatches() {
        List<Match> matches = new ArrayList<>();
        try {
            String json = fetchJson();
            JsonArray matchesArray = JsonParser.parseString(json)
                    .getAsJsonObject()
                    .getAsJsonArray("matches");
            for (JsonElement element : matchesArray) {
                matches.add(mapper.map(element.getAsJsonObject()));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al conectar con la API: " + e.getMessage());
        }
        return matches;
    }

    private String fetchJson() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Auth-Token", apiKey)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}