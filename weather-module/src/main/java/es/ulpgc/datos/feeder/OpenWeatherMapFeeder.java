package es.ulpgc.datos.feeder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.model.Weather;
import es.ulpgc.datos.model.WeatherMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapFeeder implements WeatherFeeder {

    private static final List<String> CITIES = List.of(
            "Madrid", "Barcelona", "Seville", "Valencia", "Bilbao",
            "Girona", "Pamplona", "Palma de Mallorca", "San Sebastian",
            "Villarreal", "Vigo", "Vitoria-Gasteiz", "Elche", "Oviedo"
    );

    private final WeatherMapper mapper = new WeatherMapper();
    private final String apiUrl;

    public OpenWeatherMapFeeder(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Weather> fetchWeather() {
        List<Weather> results = new ArrayList<>();
        for (String city : CITIES) {
            try {
                String json = fetchJson(city);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                results.addAll(mapper.map(jsonObject));

            } catch (IOException | InterruptedException e) {
                System.err.println("Error al obtener datos de " + city + ": " + e.getMessage());
            }
        }
        return results;
    }

    private String fetchJson(String city) throws IOException, InterruptedException {
        String url = String.format(apiUrl, city.replace(" ", "%20"));
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}