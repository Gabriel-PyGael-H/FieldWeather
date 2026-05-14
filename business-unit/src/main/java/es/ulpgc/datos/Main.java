package es.ulpgc.datos;

import es.ulpgc.datos.control.*;
import es.ulpgc.datos.model.Datamart;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        String dbPath = (args.length > 2) ? args[2] : "datamart.db";
        String eventStorePath = (args.length > 1) ? args[1] : "C:/Users/gabri/IdeaProjects/DacdTrabajo/eventstore";

        String rawBrokerUrl = (args.length > 0) ? args[0] : "localhost:61616";
        String brokerUrl = rawBrokerUrl.startsWith("tcp://") ? rawBrokerUrl : "tcp://" + rawBrokerUrl;

        Datamart datamart = new Datamart(dbPath);

        System.out.println("Cargando historial desde: " + eventStorePath);
        HistoryLoader loader = new HistoryLoader(datamart, eventStorePath);
        loader.loadFootballHistory();

        System.out.println("Conectando a ActiveMQ en: " + brokerUrl);
        EventConsumer consumer = new EventConsumer(brokerUrl, datamart);
        consumer.subscribe("Football");
        consumer.subscribe("Weather");

        Javalin app = Javalin.create().start(7000);
        new Controller(datamart).registerRoutes(app);

        System.out.println(" Business Unit lista y escuchando en el puerto 7000.");
    }
}