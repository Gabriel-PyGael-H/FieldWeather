package es.ulpgc.datos.weatherfeeder;

import es.ulpgc.datos.weatherfeeder.control.Controller;
import es.ulpgc.datos.weatherfeeder.control.feeder.OpenWeatherMapFeeder;
import es.ulpgc.datos.weatherfeeder.control.store.WeatherEventStore;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: Main <api-url> <broker-url>");
            return;
        }

        String apiUrl = args[0];
        String brokerUrl = args[1];

        Controller controller = new Controller(
                new OpenWeatherMapFeeder(apiUrl),
                new WeatherEventStore(brokerUrl)
        );
        controller.start();
    }
}