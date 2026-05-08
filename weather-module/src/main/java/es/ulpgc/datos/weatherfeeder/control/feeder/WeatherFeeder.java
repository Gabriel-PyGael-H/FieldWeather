package es.ulpgc.datos.weatherfeeder.control.feeder;

import es.ulpgc.datos.weatherfeeder.model.Weather;
import java.util.List;

public interface WeatherFeeder {

    List<Weather> fetchWeather();

}