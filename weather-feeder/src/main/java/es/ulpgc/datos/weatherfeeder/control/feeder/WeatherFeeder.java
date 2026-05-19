package es.ulpgc.datos.weatherfeeder.control.feeder;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;

import java.util.List;

public interface WeatherFeeder {

    List<WeatherEvent> fetchWeather();

}