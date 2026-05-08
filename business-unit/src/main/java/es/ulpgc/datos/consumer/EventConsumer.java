package es.ulpgc.datos.consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.datamart.Datamart;
import es.ulpgc.datos.history.HistoryLoader;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class EventConsumer {
    private final String brokerUrl;
    private final Datamart datamart;

    public EventConsumer(String brokerUrl, Datamart datamart) {
        this.brokerUrl = "failover:(" + brokerUrl + ")?maxReconnectAttempts=10";
        this.datamart = datamart;
    }

    public void subscribe(String topicName) {
        new Thread(() -> {
            try {
                Connection connection = new ActiveMQConnectionFactory(brokerUrl).createConnection();
                connection.setClientID("BU_Client_" + topicName + "_" + System.currentTimeMillis());
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(topicName);
                MessageConsumer consumer = session.createDurableSubscriber(topic, "sub-" + topicName);

                while (true) {
                    Message m = consumer.receive();
                    if (m instanceof TextMessage tm) {
                        JsonObject event = JsonParser.parseString(tm.getText()).getAsJsonObject();
                        processEvent(topicName, event);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void processEvent(String topic, JsonObject event) {
        if (topic.equals("Football")) {
            String home = event.get("homeTeam").getAsString();
            datamart.insertMatchWeather(home, event.get("awayTeam").getAsString(),
                    event.get("homeScore").getAsInt(), event.get("awayScore").getAsInt(),
                    event.get("matchDate").getAsString(), HistoryLoader.getCityForTeam(home.trim()),
                    null, null, "Live data", event.get("ts").getAsString());
        } else if (topic.equals("Weather")) {
            datamart.updateWeather(HistoryLoader.normalize(event.get("city").getAsString()),
                    event.get("temperature").getAsDouble(), event.get("humidity").getAsInt(),
                    event.get("description").getAsString(), event.get("predictionTime").getAsString());
        }
    }
}