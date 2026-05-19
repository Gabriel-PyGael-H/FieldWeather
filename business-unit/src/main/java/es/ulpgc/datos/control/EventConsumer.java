package es.ulpgc.datos.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class EventConsumer {
    private final String brokerUrl;
    private final Datamart datamart;
    private Connection connection;
    private Session session;

    public EventConsumer(String brokerUrl, Datamart datamart) {
        this.brokerUrl = brokerUrl;
        this.datamart = datamart;
        setupConnection();
    }

    private void setupConnection() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }
    }

    public void subscribe(String topicName) {
        try {
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(message -> {
                if (message instanceof TextMessage textMessage) {
                    try {
                        JsonObject event = JsonParser.parseString(textMessage.getText()).getAsJsonObject();
                        processEvent(topicName, event);
                    } catch (JMSException e) {
                        System.err.println("Error procesando mensaje: " + e.getMessage());
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void processEvent(String topic, JsonObject event) {
        try {
            if (topic.equals("Football")) {
                String home = event.get("homeTeam").getAsString();
                datamart.insertMatchWeather(
                        home,
                        event.get("awayTeam").getAsString(),
                        event.get("homeScore").getAsInt(),
                        event.get("awayScore").getAsInt(),
                        event.get("matchDate").getAsString(),
                        HistoryLoader.getCityForTeam(home.trim()),
                        null, null, "Live match data",
                        event.get("ts").getAsString()
                );
            } else if (topic.equals("Weather")) {
                datamart.updateWeather(
                        HistoryLoader.normalize(event.get("city").getAsString()),
                        event.get("temperature").getAsDouble(),
                        event.get("humidity").getAsInt(),
                        event.get("description").getAsString(),
                        event.get("predictionTime").getAsString()
                );
            }
        } catch (Exception e) {
            System.err.println("Error en la lógica de dominio: " + e.getMessage());
        }
    }
}