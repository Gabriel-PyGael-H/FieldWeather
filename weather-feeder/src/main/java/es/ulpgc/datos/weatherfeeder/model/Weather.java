package es.ulpgc.datos.weatherfeeder.model;

import java.time.LocalDateTime;

public class Weather {
    private String city;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;
    private String country;
    private LocalDateTime predictionTime;
    private String ts;

    public Weather(String city, double temperature, double feelsLike,
                   int humidity, String description, String country, LocalDateTime predictionTime, String ts) {
        this.city = city;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.country = country;
        this.predictionTime = predictionTime;
        this.ts = ts;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity()           { return city; }
    public double getTemperature()    { return temperature; }
    public double getFeelsLike()      { return feelsLike; }
    public int getHumidity()          { return humidity; }
    public String getDescription()    { return description; }
    public String getCountry()        { return country; }
    public LocalDateTime getPredictionTime() {return predictionTime;}
    public String getTs() { return ts; }

    @Override
    public String toString() {
        return city + " (" + country + ") - " + temperature + "°C, " +
                description + " | Captured: " + ts;
    }

}