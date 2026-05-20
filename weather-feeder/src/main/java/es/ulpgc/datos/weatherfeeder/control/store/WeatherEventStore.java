package es.ulpgc.datos.weatherfeeder.control.store;

import com.google.gson.Gson;
import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;

public class WeatherEventStore implements WeatherStore {
    private static final String TOPIC_NAME = "Weather";

    private final Gson gson = new Gson();
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public WeatherEventStore(String brokerUrl) {
        setupActiveMQ(brokerUrl);
    }

    private void setupActiveMQ(String brokerUrl) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(TOPIC_NAME);
            this.producer = session.createProducer(destination);
        } catch (JMSException e) {
            System.err.println("Error al inicializar la conexión con ActiveMQ: " + e.getMessage());
        }
    }

    @Override
    public void store(List<WeatherEvent> weatherEvents) {
        if (producer == null || session == null) return;

        try {
            for (WeatherEvent event : weatherEvents) {
                sendEvent(event);
            }
        } catch (JMSException e) {
            System.err.println("Error al publicar lote en ActiveMQ: " + e.getMessage());
        }
    }

    private void sendEvent(WeatherEvent event) throws JMSException {
        String json = gson.toJson(event);
        TextMessage message = session.createTextMessage(json);
        producer.send(message);
    }
}