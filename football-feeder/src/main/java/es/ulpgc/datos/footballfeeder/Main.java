package es.ulpgc.datos.footballfeeder;

import es.ulpgc.datos.footballfeeder.control.Controller;
import es.ulpgc.datos.footballfeeder.control.feeder.FootballDataFeeder;
import es.ulpgc.datos.footballfeeder.control.store.MatchEventStore;

public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: Main <api-url> <api-key> <database-name> <broker-url>");
            return;
        }

        String apiUrl = args[0];
        String apiKey = args[1];
        String brokerUrl = args[2];

        Controller controller = new Controller(
                new FootballDataFeeder(apiUrl, apiKey),
                new MatchEventStore(brokerUrl)

        );
        controller.start();
    }
}