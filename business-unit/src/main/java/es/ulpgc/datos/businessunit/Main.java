package es.ulpgc.datos.businessunit;

import es.ulpgc.datos.businessunit.control.*;
import es.ulpgc.datos.businessunit.control.eventprocessors.FootballProcessor;
import es.ulpgc.datos.businessunit.control.eventprocessors.WeatherProcessor;
import es.ulpgc.datos.businessunit.view.UIService;

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

        FootballProcessor footballProcessor = new FootballProcessor(datamart);
        WeatherProcessor weatherProcessor = new WeatherProcessor(datamart);
        HistoryLoader loader = new HistoryLoader(footballProcessor, weatherProcessor, eventStorePath);
        loader.loadFootballHistory();
        EventConsumer consumer = new EventConsumer(brokerUrl, footballProcessor, weatherProcessor);
        consumer.subscribe("Football");
        consumer.subscribe("Weather");

        new UIService(datamart).start(port);

        Object lock = new Object();
        synchronized (lock) { lock.wait(); }
    }
}