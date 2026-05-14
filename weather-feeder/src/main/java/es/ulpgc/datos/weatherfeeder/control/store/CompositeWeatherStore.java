package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent; // Importamos la clase fusionada
import java.util.List;

public class CompositeWeatherStore implements WeatherStore {

    private final WeatherStore dbStore;
    private final WeatherStore eventStore;

    public CompositeWeatherStore(WeatherStore dbStore, WeatherStore eventStore) {
        this.dbStore = dbStore;
        this.eventStore = eventStore;
    }

    @Override
    public void store(List<WeatherEvent> weatherList) { // <--- CAMBIO: Ahora recibe WeatherEvent
        dbStore.store(weatherList);
        eventStore.store(weatherList);
    }
}