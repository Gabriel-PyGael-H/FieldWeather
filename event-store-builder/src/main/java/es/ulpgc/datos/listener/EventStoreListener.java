package es.ulpgc.datos.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.store.EventStore;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class EventStoreListener {

    private static final String CLIENT_ID = "EventStoreBuilder_Global";
    private final String brokerUrl;
    private final EventStore eventStore;

    public EventStoreListener(EventStore eventStore, String brokerUrl) {
        this.eventStore = eventStore;
        this.brokerUrl = "failover:(" + brokerUrl + ")?maxReconnectAttempts=10";
    }

    public void subscribe(String topicName) {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
                    Connection connection = factory.createConnection();
                    connection.setClientID(CLIENT_ID + "_" + topicName);
                    connection.start();

                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Topic topic = session.createTopic(topicName);
                    MessageConsumer consumer = session.createDurableSubscriber(topic, "sub-" + topicName);

                    System.out.println("Suscrito a: " + topicName);

                    while (true) {
                        Message message = consumer.receive();
                        if (message instanceof TextMessage textMessage) {
                            String json = textMessage.getText();
                            JsonObject event = JsonParser.parseString(json).getAsJsonObject();

                            // Extraemos metadatos para organizar el archivo
                            String ts = event.get("ts").getAsString();
                            String ss = event.get("ss").getAsString();

                            eventStore.store(topicName, ss, ts, json);
                        }
                    }
                } catch (JMSException e) {
                    System.err.println("Reconectando " + topicName + "... " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                }
            }
        }).start();
    }
}