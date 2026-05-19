package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import java.util.List;

public class CompositeWeatherStore implements WeatherStore {

    private final WeatherStore dbStore;
    private final WeatherStore eventStore;

    public CompositeWeatherStore(WeatherStore dbStore, WeatherStore eventStore) {
        this.dbStore = dbStore;
        this.eventStore = eventStore;
    }

    @Override
    public void store(List<WeatherEvent> weatherEvents) {
        dbStore.store(weatherEvents);
        eventStore.store(weatherEvents);
    }
}