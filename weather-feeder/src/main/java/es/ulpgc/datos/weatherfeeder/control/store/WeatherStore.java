package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;

import java.util.List;

public interface WeatherStore {

    void store(List<WeatherEvent> weatherList);

}