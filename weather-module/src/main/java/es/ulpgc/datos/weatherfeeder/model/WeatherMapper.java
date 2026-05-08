package es.ulpgc.datos.weatherfeeder.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class WeatherMapper {

    private static final DateTimeFormatter API_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Weather> map(JsonObject json) {
        List<Weather> forecasts = new ArrayList<>();
        String ts = Instant.now().toString();

        JsonObject cityObject = json.getAsJsonObject("city");
        String city = cityObject.get("name").getAsString();
        String country = cityObject.get("country").getAsString();

        JsonArray list = json.getAsJsonArray("list");

        for (JsonElement element : list) {
            JsonObject f = element.getAsJsonObject();

            String dtTxt = f.get("dt_txt").getAsString();
            LocalDateTime dateTime = LocalDateTime.parse(dtTxt, API_FORMATTER)
                    .withSecond(0)
                    .withNano(0);

            JsonObject main = f.getAsJsonObject("main");
            double temperature = main.get("temp").getAsDouble();
            double feelsLike = main.get("feels_like").getAsDouble();
            int humidity = main.get("humidity").getAsInt();

            String description = f.getAsJsonArray("weather")
                    .get(0).getAsJsonObject()
                    .get("description").getAsString();

            forecasts.add(new Weather(city, temperature, feelsLike, humidity,
                    description, country, dateTime, ts));
        }

        return forecasts;
    }
}