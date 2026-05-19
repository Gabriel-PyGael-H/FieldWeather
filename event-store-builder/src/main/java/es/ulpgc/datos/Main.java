package es.ulpgc.datos;

import es.ulpgc.datos.listener.EventStoreListener;
import es.ulpgc.datos.store.EventStore;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            System.out.println("Uso: Main <broker-url> <eventstore-path>");
            return;
        }

        String brokerUrl = args[0];
        String eventStorePath = args[1];

        EventStore eventStore = new EventStore(eventStorePath);
        EventStoreListener listener = new EventStoreListener(eventStore, brokerUrl);
        listener.subscribe("Football");
        listener.subscribe("Weather");

        System.out.println("Event Store Builder iniciado. Esperando eventos...");

        while (true) {
            Thread.sleep(5000);
        }
    }
}