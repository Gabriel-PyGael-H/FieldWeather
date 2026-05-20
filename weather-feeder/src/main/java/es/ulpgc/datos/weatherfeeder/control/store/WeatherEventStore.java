package es.ulpgc.datos.weatherfeeder.control.store;

import com.google.gson.Gson;
import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public class WeatherEventStore implements WeatherStore {
    private static final String TOPIC_NAME = "Weather";

    private final Gson gson;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public WeatherEventStore(String brokerUrl) {
        this.gson = new Gson();
        initActiveMQ(brokerUrl);
    }

    private void initActiveMQ(String brokerUrl) {
        try {
            setupResources(brokerUrl);
        } catch (JMSException e) {
            System.err.println("Error initializing connection with ActiveMQ: " + e.getMessage());
        }
    }
    private void setupResources(String brokerUrl) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.start();
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(TOPIC_NAME);
        this.producer = session.createProducer(destination);
    }

    @Override
    public void store(List<WeatherEvent> weatherEvents) {
        if (isNotReady()) return;

        try {
            publishEvents(weatherEvents);
        } catch (JMSException e) {
            System.err.println("Error publishing batch to ActiveMQ: " + e.getMessage());
        }
    }

    private boolean isNotReady() {
        return producer == null || session == null;
    }

    private void publishEvents(List<WeatherEvent> weatherEvents) throws JMSException {
        for (WeatherEvent event : weatherEvents) {
            sendEvent(event);
        }
    }

    private void sendEvent(WeatherEvent event) throws JMSException {
        String json = gson.toJson(event);
        TextMessage message = session.createTextMessage(json);
        producer.send(message);
    }
}