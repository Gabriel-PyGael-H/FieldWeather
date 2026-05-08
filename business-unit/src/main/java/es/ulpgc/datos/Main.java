package es.ulpgc.datos;

import es.ulpgc.datos.api.RestApi;
import es.ulpgc.datos.consumer.EventConsumer;
import es.ulpgc.datos.datamart.Datamart;
import es.ulpgc.datos.history.HistoryLoader;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) return;

        Datamart datamart = new Datamart(args[2]);

        new HistoryLoader(datamart, args[1]).loadFootballHistory();

        EventConsumer consumer = new EventConsumer(args[0], datamart);
        consumer.subscribe("Football");
        consumer.subscribe("Weather");

        new RestApi(datamart).start(7070);
    }
}