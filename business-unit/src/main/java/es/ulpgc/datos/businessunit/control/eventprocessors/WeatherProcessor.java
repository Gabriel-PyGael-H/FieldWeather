package es.ulpgc.datos.businessunit.control.eventprocessors;

import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.model.Recommendation;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class WeatherProcessor {
    private static final String DEFAULT_DESCRIPTION = "Live match data";
    private final Datamart datamart;

    public WeatherProcessor(Datamart datamart) {
        this.datamart = datamart;
    }

    public void processEvent(JsonObject event) {
        try {
            executeEventProcessing(event);
        } catch (Exception e) {
            System.err.println("Error processing weather event: " + e.getMessage());
        }
    }

    private void executeEventProcessing(JsonObject event) {
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
    }

    private double calculateFinalTemperature(JsonObject currentData, double newTemp, String newTime) {
        if (currentData == null || !currentData.has("predictionTime") || currentData.get("predictionTime").isJsonNull()) {
            return newTemp;
        }

        String matchDate = currentData.get("matchDate").getAsString();
        double currentTemp = currentData.get("temperature").getAsDouble();
        String currentPred = currentData.get("predictionTime").getAsString();

        return tryInterpolation(currentTemp, currentPred, newTemp, newTime, matchDate);
    }

    private double tryInterpolation(double t1, String time1, double t2, String time2, String tMatch) {
        try {
            return interpolate(t1, time1, t2, time2, tMatch);
        } catch (DateTimeParseException | ArithmeticException e) {
            return t2;
        }
    }

    private double interpolate(double t1, String time1, double t2, String time2, String tMatch) {
        long e1 = parseToEpoch(time1);
        long e2 = parseToEpoch(time2);
        long em = parseToEpoch(tMatch);

        if (e1 == e2) {
            return t2;
        }
        return t1 + (t2 - t1) * (double) (em - e1) / (e2 - e1);
    }

    private long parseToEpoch(String t) {
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
        if (temp < 5)  return new Recommendation("SNOW AND COLD: Thermal clothing required.", "CRITICAL");
        if (temp < 15) return new Recommendation("RAIN AND COLD: Raincoat recommended.", "DANGER");
        return new Recommendation("RAIN: Umbrella required.", "WARNING");
    }

    private Recommendation buildStandardRecommendation(double temp, String d) {
        if (temp <= 0)  return new Recommendation("FREEZING: Heavy winter coat required.", "CRITICAL");
        if (temp <= 10) return new Recommendation("INTENSE COLD: Winter clothing required.", "COLD");
        if (temp <= 16) return new Recommendation("COLD: Jacket recommended.", "CHILLY");
        if (temp <= 22) return new Recommendation("COOL: Light jacket recommended.", "CHILLY");
        if (temp <= 28) return new Recommendation("MILD: No coat needed, bring one just in case.", "CHILLY");
        if (temp <= 35) return new Recommendation("INTENSE HEAT: Stay hydrated.", "HOT");
        return new Recommendation("HEAT ALERT: Avoid direct sunlight.", "CRITICAL");
    }
}