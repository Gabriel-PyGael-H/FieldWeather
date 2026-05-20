package es.ulpgc.datos.weatherfeeder.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WeatherMapper {
    private static final String SOURCE_ID = "weather-feeder-v1";

    public List<WeatherEvent> map(JsonObject json, String city) {
        List<WeatherEvent> forecasts = new ArrayList<>();
        String ts = Instant.now().toString();

        JsonObject cityObject = json.getAsJsonObject("city");
        String country = cityObject.get("country").getAsString();

        JsonArray list = json.getAsJsonArray("list");
        for (JsonElement element : list) {
            forecasts.add(createWeatherEvent(element.getAsJsonObject(), ts, city, country));
        }

        return forecasts;
    }

    private WeatherEvent createWeatherEvent(JsonObject f, String ts, String city, String country) {
        JsonObject main = f.getAsJsonObject("main");
        double temperature = main.get("temp").getAsDouble();
        double feelsLike = main.get("feels_like").getAsDouble();
        int humidity = main.get("humidity").getAsInt();

        String description = extractDescription(f);
        String dtTxt = f.get("dt_txt").getAsString();
        return new WeatherEvent(
                ts, SOURCE_ID, city, country,
                temperature, feelsLike, humidity,
                description, dtTxt
        );
    }

    private String extractDescription(JsonObject f) {
        return f.getAsJsonArray("weather")
                .get(0).getAsJsonObject()
                .get("description").getAsString();
    }
}