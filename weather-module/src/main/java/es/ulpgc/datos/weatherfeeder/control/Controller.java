package es.ulpgc.datos.weatherfeeder.control;

import es.ulpgc.datos.weatherfeeder.control.feeder.WeatherFeeder;
import es.ulpgc.datos.weatherfeeder.model.Weather;
import es.ulpgc.datos.weatherfeeder.control.store.WeatherStore;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private final WeatherFeeder feeder;
    private final WeatherStore store;

    public Controller(WeatherFeeder feeder, WeatherStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::fetch, 0, 1, TimeUnit.HOURS);
        System.out.println("Scheduler iniciado. Capturando datos cada hora...");
    }

    private void fetch() {
        System.out.println("Obteniendo datos meteorológicos...");
        List<Weather> weatherList = feeder.fetchWeather();
        System.out.println("Ciudades obtenidas: " + weatherList.size());
        weatherList.forEach(System.out::println);
        store.store(weatherList);
    }
}
