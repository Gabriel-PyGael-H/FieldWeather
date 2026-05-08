package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.Weather;
import java.util.List;

public class CompositeWeatherStore implements WeatherStore {

    private final WeatherStore dbStore;
    private final WeatherStore eventStore;

    public CompositeWeatherStore(WeatherStore dbStore, WeatherStore eventStore) {
        this.dbStore = dbStore;
        this.eventStore = eventStore;
    }

    @Override
    public void store(List<Weather> weatherList) {
        dbStore.store(weatherList);
        eventStore.store(weatherList);
    }
}