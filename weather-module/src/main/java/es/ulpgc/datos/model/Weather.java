package es.ulpgc.datos.model;

import java.time.LocalDateTime;

public class Weather {
    private String city;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;
    private String country;
    private LocalDateTime predictionTime;

    public Weather(String city, double temperature, double feelsLike,
                   int humidity, String description, String country, LocalDateTime predictionTime) {
        this.city = city;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.country = country;
        this.predictionTime = predictionTime;
    }

    public String getCity()           { return city; }
    public double getTemperature()    { return temperature; }
    public double getFeelsLike()      { return feelsLike; }
    public int getHumidity()          { return humidity; }
    public String getDescription()    { return description; }
    public String getCountry()        { return country; }
    public LocalDateTime getPredictionTime() {return predictionTime;}

    @Override
    public String toString() {
        return city + " (" + country + ") - " + temperature + "°C, " + description
                + ", Humidity: " + humidity + "%";
    }
}