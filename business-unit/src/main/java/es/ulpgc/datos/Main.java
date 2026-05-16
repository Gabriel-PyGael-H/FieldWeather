package es.ulpgc.datos;

import es.ulpgc.datos.control.*;
import es.ulpgc.datos.model.Datamart;
import es.ulpgc.datos.view.RestApi;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("Uso: Main <broker-url> <eventstore-path> <port>");
            return;
        }

        String brokerUrl      = args[0];
        String eventStorePath = args[1];
        int    port           = Integer.parseInt(args[2]);

        brokerUrl = brokerUrl.startsWith("tcp://") ? brokerUrl : "tcp://" + brokerUrl;

        Datamart datamart = new Datamart("datamart.db");

        HistoryLoader loader = new HistoryLoader(datamart, eventStorePath);
        loader.loadFootballHistory();

        EventConsumer consumer = new EventConsumer(brokerUrl, datamart);
        consumer.subscribe("Football");
        consumer.subscribe("Weather");

        new RestApi(datamart).start(port);

        Object lock = new Object();
        synchronized (lock) { lock.wait(); }
    }
}