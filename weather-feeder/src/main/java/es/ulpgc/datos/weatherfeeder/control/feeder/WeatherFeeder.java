package es.ulpgc.datos.weatherfeeder.control.feeder;

import java.util.List;

public interface WeatherFeeder {

    List<Weather> fetchWeather();

}