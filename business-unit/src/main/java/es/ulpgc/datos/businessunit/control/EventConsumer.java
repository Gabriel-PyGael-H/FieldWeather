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

    public EventConsumer(String brokerUrl, FootballProcessor footballProcessor, WeatherProcessor weatherProcessor) {
        this.brokerUrl = brokerUrl;
        this.footballProcessor = footballProcessor;
        this.weatherProcessor = weatherProcessor;
        setupConnection();
    }

    private void setupConnection() {
        try {
            initActiveMQ();
        } catch (JMSException e) {
            System.err.println("Error connecting to ActiveMQ: " + e.getMessage());
        }
    }

    private void initActiveMQ() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void subscribe(String topicName) {
        try {
            registerTopicSubscriber(topicName);
        } catch (JMSException e) {
            System.err.println("Error subscribing to topic " + topicName + ": " + e.getMessage());
        }
    }

    private void registerTopicSubscriber(String topicName) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(message -> handleMessage(message, topicName));
    }

    private void handleMessage(Message message, String topicName) {
        if (message instanceof TextMessage textMessage) {
            processIncomingMessage(textMessage, topicName);
        }
    }

    private void processIncomingMessage(TextMessage textMessage, String topicName) {
        try {
            dispatchMessageEvent(textMessage, topicName);
        } catch (Exception e) {
            System.err.println("Error processing ActiveMQ message: " + e.getMessage());
        }
    }

    private void dispatchMessageEvent(TextMessage textMessage, String topicName) throws Exception {
        JsonObject event = JsonParser.parseString(textMessage.getText()).getAsJsonObject();
        routeEventToProcessor(event, topicName);
    }

    private void routeEventToProcessor(JsonObject event, String topicName) {
        if (topicName.equals("Football")) {
            footballProcessor.processEvent(event);
        } else if (topicName.equals("Weather")) {
            weatherProcessor.processEvent(event);
        }
    }
}