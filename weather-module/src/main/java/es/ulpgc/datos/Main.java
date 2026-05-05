package es.ulpgc.datos;

import es.ulpgc.datos.feeder.OpenWeatherMapFeeder;
import es.ulpgc.datos.store.WeatherEventStore;

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