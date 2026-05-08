package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.Weather;
import java.util.List;

public interface WeatherStore {

    void store(List<Weather> weatherList);

}