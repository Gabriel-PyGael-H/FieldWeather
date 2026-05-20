package es.ulpgc.datos.weatherfeeder.control.feeder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import es.ulpgc.datos.weatherfeeder.model.WeatherMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapFeeder implements WeatherFeeder {
    private static final List<String> CITIES = List.of(
            "Madrid", "Barcelona", "Sevilla", "Valencia", "Bilbao",
            "Girona", "Pamplona", "Palma de Mallorca", "San Sebastian",
            "Castellón", "Vigo", "Vitoria-Gasteiz", "Elche", "Oviedo"
    );

    private final WeatherMapper mapper = new WeatherMapper();
    private final String apiUrl;
    private final HttpClient client;

    public OpenWeatherMapFeeder(String apiUrl) {
        this.apiUrl = apiUrl;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public List<WeatherEvent> fetchWeather() {
        List<WeatherEvent> results = new ArrayList<>();
        for (String city : CITIES) {
            results.addAll(fetchCityWeather(city));
        }
        return results;
    }

    private List<WeatherEvent> fetchCityWeather(String city) {
        try {
            String json = fetchJson(city);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return mapper.map(jsonObject, city);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al obtener datos de " + city + ": " + e.getMessage());
            return List.of();
        }
    }

    private String fetchJson(String city) throws IOException, InterruptedException {
        String url = String.format(apiUrl, city.replace(" ", "%20"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}