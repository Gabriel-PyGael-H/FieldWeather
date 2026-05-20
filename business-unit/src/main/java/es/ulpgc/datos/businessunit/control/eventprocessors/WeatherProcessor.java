package es.ulpgc.datos.businessunit.control.eventprocessors;

import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.model.Recommendation;
import java.time.Instant;

public class WeatherProcessor {
    private static final String DEFAULT_DESCRIPTION = "Live match data";
    private final Datamart datamart;

    public WeatherProcessor(Datamart datamart) {
        this.datamart = datamart;
    }

    public void processEvent(JsonObject event) {
        try {
            String city = event.get("city").getAsString().trim();
            double newTemp = event.get("temperature").getAsDouble();
            int hum = event.get("humidity").getAsInt();
            String desc = event.get("description").getAsString();
            String newTime = event.get("predictionTime").getAsString();
            String dayFilter = newTime.substring(0, 10) + "%";

            JsonObject currentData = datamart.getMatchDataForInterpolation(city, dayFilter);
            double finalTemp = calculateFinalTemperature(currentData, newTemp, newTime);

            Recommendation rec = buildRecommendation(finalTemp, desc);
            datamart.updateWeather(city, finalTemp, hum, desc, newTime, rec, dayFilter);
        } catch (Exception e) {
            System.err.println("Error en WeatherProcessor: " + e.getMessage());
        }
    }

    private double calculateFinalTemperature(JsonObject currentData, double newTemp, String newTime) {
        if (currentData == null || !currentData.has("predictionTime") || currentData.get("predictionTime").isJsonNull()) {
            return newTemp;
        }

        String matchDate = currentData.get("matchDate").getAsString();
        double currentTemp = currentData.get("temperature").getAsDouble();
        String currentPred = currentData.get("predictionTime").getAsString();

        return interpolate(currentTemp, currentPred, newTemp, newTime, matchDate);
    }

    private double interpolate(double t1, String time1, double t2, String time2, String tMatch) {
        try {
            long e1 = parseToEpoch(time1);
            long e2 = parseToEpoch(time2);
            long em = parseToEpoch(tMatch);
            return (e1 == e2) ? t2 : t1 + (t2 - t1) * (double) (em - e1) / (e2 - e1);
        } catch (Exception e) {
            return t2;
        }
    }

    private long parseToEpoch(String t) {
        // Corregido: Parseo nativo y seguro para formatos ISO (ej. "2026-05-06T21:00:00Z") sin hacer .replace()
        return Instant.parse(t).getEpochSecond();
    }

    private Recommendation buildRecommendation(double temp, String desc) {
        String d = desc.toLowerCase();
        if (isRainy(d)) {
            return buildRainyRecommendation(temp);
        }
        return buildStandardRecommendation(temp, d);
    }

    private boolean isRainy(String desc) {
        return desc.contains("rain") || desc.contains("drizzle") || desc.contains("thunderstorm") || desc.contains("snow");
    }

    private Recommendation buildRainyRecommendation(double temp) {
        if (temp < 5)  return new Recommendation("NIEVE Y FRÍO: Ropa térmica.", "CRITICAL");
        if (temp < 15) return new Recommendation("LLUVIA Y FRÍO: Chubasquero.", "DANGER");
        return new Recommendation("LLUVIA: Paraguas.", "WARNING");
    }

    private Recommendation buildStandardRecommendation(double temp, String d) {
        if (temp <= 0)  return new Recommendation("HELADA: Abrigo de montaña.", "CRITICAL");
        if (temp <= 10) return new Recommendation("FRÍO INTENSO: Ropa de invierno.", "COLD");
        if (temp <= 16) return new Recommendation("FRÍO: Chaqueta.", "CHILLY");
        if (temp <= 22) return new Recommendation("FRESCO: Chaqueta.", "CHILLY");
        if (temp <= 28) return new Recommendation("AGRADABLE: NO HACE FALTA ABRIGO, LLEVA POR SI ACASO.", "CHILLY");
        if (temp <= 35) return new Recommendation("CALOR INTENSO: Hidrátate.", "HOT");
        return new Recommendation("ALERTA CALOR: Evita el sol.", "CRITICAL");
    }

    private Recommendation buildMildDayRecommendation(String d) {
        boolean isCloudy = d.contains("clouds") || d.contains("mist") || d.contains("fog");
        return isCloudy ? new Recommendation("DÍA FRESCO: Rebeca fina.", "INFO")
                : new Recommendation("TIEMPO PARA LLEVAR ABRIGO POR SI ACASO: Manga corta.", "PERFECT");
    }

    private Recommendation buildWarmDayRecommendation(String d) {
        boolean isSunny = d.contains("clear") || d.contains("sun");
        return isSunny ? new Recommendation("SOLEADO: Gafas de sol.", "SUNNY")
                : new Recommendation("AGRADABLE: Ropa ligera.", "WARM");
    }
}