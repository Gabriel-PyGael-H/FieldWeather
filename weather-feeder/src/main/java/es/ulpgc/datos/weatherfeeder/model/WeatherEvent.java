package es.ulpgc.datos.weatherfeeder.model;

public class WeatherEvent {
    private final String ts;
    private final String ss;

    private final String city;
    private final String country;

    private final double temperature;
    private final double feelsLike;
    private final int humidity;
    private final String description;

    private final String predictionTime;

    public WeatherEvent(String ts, String ss, String city, String country,
                        double temperature, double feelsLike, int humidity,
                        String description, String predictionTime) {
        this.ts = ts;
        this.ss = ss;
        this.city = city;
        this.country = country;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.predictionTime = predictionTime;
    }

    public String getTs() { return ts; }
    public String getSs() { return ss; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getHumidity() { return humidity; }
    public String getDescription() { return description; }
    public String getPredictionTime() { return predictionTime; }
}