package es.ulpgc.datos.businessunit;

import es.ulpgc.datos.businessunit.control.Datamart;
import es.ulpgc.datos.businessunit.control.EventConsumer;
import es.ulpgc.datos.businessunit.control.HistoryLoader;
import es.ulpgc.datos.businessunit.control.eventprocessors.FootballProcessor;
import es.ulpgc.datos.businessunit.control.eventprocessors.WeatherProcessor;
import es.ulpgc.datos.businessunit.view.UIService;

public class Main {
    public static void main(String[] args) {
        String brokerUrl = args[0];
        String eventStorePath = args[1];
        int webPort = Integer.parseInt(args[2]);

        Datamart datamart = new Datamart("datamart.db");
        FootballProcessor footballProcessor = new FootballProcessor(datamart);
        WeatherProcessor weatherProcessor = new WeatherProcessor(datamart);

        HistoryLoader historyLoader = new HistoryLoader(footballProcessor, weatherProcessor, eventStorePath);
        System.out.println("Iniciando carga del histórico...");
        historyLoader.loadFootballHistory();

        EventConsumer consumer = new EventConsumer(brokerUrl, footballProcessor, weatherProcessor);
        consumer.subscribe("Football");
        consumer.subscribe("Weather");

        System.out.println("Suscrito a los tópicos de ActiveMQ en tiempo real.");
        UIService uiService = new UIService(datamart);
        uiService.start(webPort);
    }
}