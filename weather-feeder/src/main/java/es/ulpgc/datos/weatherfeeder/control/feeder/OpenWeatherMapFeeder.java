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

    public OpenWeatherMapFeeder(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<WeatherEvent> fetchWeather() {
        List<WeatherEvent> results = new ArrayList<>();
        for (String city : CITIES) {
            try {
                String json = fetchJson(city);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

                List<WeatherEvent> weatherEvents = mapper.map(jsonObject);
                for (WeatherEvent weather : weatherEvents) {
                    weather.setCity(city);
                }

                results.addAll(weatherEvents);

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