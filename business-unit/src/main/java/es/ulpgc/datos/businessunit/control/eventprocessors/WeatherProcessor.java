package es.ulpgc.datos.businessunit.control.eventprocessors;

import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.control.HistoryLoader;
import es.ulpgc.datos.businessunit.model.Recommendation;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class WeatherProcessor {
    private final Datamart datamart;

    public WeatherProcessor(Datamart datamart) {
        this.datamart = datamart;
    }

    public void processEvent(JsonObject event) {
        try {
            String city = HistoryLoader.normalize(event.get("city").getAsString());
            double newTemp = event.get("temperature").getAsDouble();
            int hum = event.get("humidity").getAsInt();
            String desc = event.get("description").getAsString();
            String newTime = event.get("predictionTime").getAsString();
            String dayFilter = newTime.substring(0, 10) + "%";

            JsonObject currentData = datamart.getMatchDataForInterpolation(city, dayFilter);
            double finalTemp = newTemp;

            if (currentData != null) {
                String matchDate = currentData.get("matchDate").getAsString();
                double currentTemp = currentData.get("temperature").getAsDouble();
                String currentPred = currentData.has("predictionTime") && !currentData.get("predictionTime").isJsonNull()
                        ? currentData.get("predictionTime").getAsString() : null;

                if (currentPred != null) {
                    finalTemp = interpolate(currentTemp, currentPred, newTemp, newTime, matchDate);
                }
            }

            Recommendation rec = buildRecommendation(finalTemp, desc);
            datamart.updateWeather(city, finalTemp, hum, desc, newTime, rec, dayFilter);
        } catch (Exception e) {
            System.err.println("Error en WeatherProcessor: " + e.getMessage());
        }
    }
    private double interpolate(double t1, String time1, double t2, String time2, String tMatch) {
        try {
            long e1 = parseToEpoch(time1), e2 = parseToEpoch(time2), em = parseToEpoch(tMatch);
            return (e1 == e2) ? t2 : t1 + (t2 - t1) * (double)(em - e1) / (e2 - e1);
        } catch (Exception e) { return t2; }
    }

    private long parseToEpoch(String t) {
        return LocalDateTime.parse(t.replace("Z","").replace("T"," "),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC);
    }

    private Recommendation buildRecommendation(double temp, String desc) {
        String d = desc.toLowerCase();
        boolean isRainy  = d.contains("rain") || d.contains("drizzle") || d.contains("thunderstorm") || d.contains("snow");
        boolean isCloudy = d.contains("clouds") || d.contains("mist") || d.contains("fog");

        if (isRainy) {
            if (temp < 5)  return new Recommendation("NIEVE Y FRÍO: Ropa térmica.", "CRITICAL");
            if (temp < 15) return new Recommendation("LLUVIA Y FRÍO: Chubasquero.", "DANGER");
            return new Recommendation("LLUVIA: Paraguas.", "WARNING");
        }
        if (temp <= 0)  return new Recommendation("HELADA: Abrigo de montaña.", "CRITICAL");
        if (temp <= 10) return new Recommendation("FRÍO INTENSO: Ropa de invierno.", "COLD");
        if (temp <= 16) return new Recommendation("FRÍO: Chaqueta.", "CHILLY");
        if (temp <= 22) {
            return isCloudy ? new Recommendation("DÍA FRESCO: Rebeca fina.", "INFO")
                    : new Recommendation("TIEMPO PARA LLEVAR ABRIGO POR SI ACASO: Manga corta.", "PERFECT");
        }
        if (temp <= 28) {
            return (d.contains("clear") || d.contains("sun")) ? new Recommendation("SOLEADO: Gafas de sol.", "SUNNY")
                    : new Recommendation("AGRADABLE: Ropa ligera.", "WARM");
        }
        if (temp <= 35) return new Recommendation("CALOR INTENSO: Hidrátate.", "HOT");
        return new Recommendation("ALERTA CALOR: Evita el sol.", "CRITICAL");
    }
}