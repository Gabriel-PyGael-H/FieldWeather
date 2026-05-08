package es.ulpgc.datos.weatherfeeder.control.store;

import com.google.gson.Gson;
import es.ulpgc.datos.weatherfeeder.model.Weather;
import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.Instant;
import java.util.List;

public class WeatherEventStore implements WeatherStore {

    private static final String TOPIC_NAME = "Weather";
    private static final String SOURCE_ID = "weather-feeder-v1";

    private final String brokerUrl;
    private final Gson gson = new Gson();

    public WeatherEventStore(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    @Override
    public void store(List<Weather> weatherList) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(TOPIC_NAME);
            MessageProducer producer = session.createProducer(destination);

            for (Weather weather : weatherList) {
                String ts = Instant.now().toString();

                WeatherEvent event = new WeatherEvent(
                        ts,
                        SOURCE_ID,
                        weather.getCity(),
                        weather.getCountry(),
                        weather.getTemperature(),
                        weather.getFeelsLike(),
                        weather.getHumidity(),
                        weather.getDescription(),
                        weather.getPredictionTime().toString()
                );

                String json = gson.toJson(event);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
            }
            System.out.println("Publicados " + weatherList.size() + " eventos de clima en el topic " + TOPIC_NAME);

            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("Error al publicar en ActiveMQ: " + e.getMessage());
        }
    }
}