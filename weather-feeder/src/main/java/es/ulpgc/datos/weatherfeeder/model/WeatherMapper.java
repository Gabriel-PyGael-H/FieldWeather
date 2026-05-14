package es.ulpgc.datos.weatherfeeder.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherMapper {

    private static final DateTimeFormatter API_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SOURCE_ID = "weather-feeder-v1";

    public List<WeatherEvent> map(JsonObject json) {
        List<WeatherEvent> forecasts = new ArrayList<>();
        String ts = Instant.now().toString();

        JsonObject cityObject = json.getAsJsonObject("city");
        String city = cityObject.get("name").getAsString();
        String country = cityObject.get("country").getAsString();

        JsonArray list = json.getAsJsonArray("list");

        for (JsonElement element : list) {
            JsonObject f = element.getAsJsonObject();

            String dtTxt = f.get("dt_txt").getAsString();

            JsonObject main = f.getAsJsonObject("main");
            double temperature = main.get("temp").getAsDouble();
            double feelsLike = main.get("feels_like").getAsDouble();
            int humidity = main.get("humidity").getAsInt();

            String description = f.getAsJsonArray("weather")
                    .get(0).getAsJsonObject()
                    .get("description").getAsString();

            forecasts.add(new WeatherEvent(
                    ts,
                    SOURCE_ID,
                    city,
                    country,
                    temperature,
                    feelsLike,
                    humidity,
                    description,
                    dtTxt
            ));
        }

        return forecasts;
    }
}