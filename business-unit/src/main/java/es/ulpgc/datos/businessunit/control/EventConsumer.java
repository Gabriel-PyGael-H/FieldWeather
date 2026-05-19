package es.ulpgc.datos.businessunit.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.businessunit.control.eventprocessors.FootballProcessor;
import es.ulpgc.datos.businessunit.control.eventprocessors.WeatherProcessor;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class EventConsumer {
    private final String brokerUrl;
    private final FootballProcessor footballProcessor;
    private final WeatherProcessor weatherProcessor;
    private Connection connection;
    private Session session;

    public EventConsumer(String brokerUrl, FootballProcessor fp, WeatherProcessor wp) {
        this.brokerUrl = brokerUrl;
        this.footballProcessor = fp;
        this.weatherProcessor = wp;
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
                        if (topicName.equals("Football")) {
                            footballProcessor.processEvent(event);
                        } else if (topicName.equals("Weather")) {
                            weatherProcessor.processEvent(event);
                        }

                    } catch (JMSException e) {
                        System.err.println("Error procesando mensaje de ActiveMQ: " + e.getMessage());
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}