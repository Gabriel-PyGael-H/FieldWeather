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
        new Thread(() -> runSubscriptionLoop(topicName)).start();
    }

    private void runSubscriptionLoop(String topicName) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                connectAndListen(topicName);
            } catch (JMSException e) {
                handleConnectionFailure(topicName, e);
            }
        }
    }
    private void connectAndListen(String topicName) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            connection.setClientID(CLIENT_ID + "_" + topicName);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            consumer = session.createDurableSubscriber(topic, "sub-" + topicName);

            System.out.println("Subscribed to: " + topicName);
            listenForMessages(consumer, topicName);
        } finally {
            closeResources(consumer, session, connection);
        }
    }
    private void listenForMessages(MessageConsumer consumer, String topicName) throws JMSException {
        while (!Thread.currentThread().isInterrupted()) {
            Message message = consumer.receive();
            processMessage(message, topicName);
        }
    }
    private void processMessage(Message message, String topicName) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            String json = textMessage.getText();
            storeJsonEvent(json, topicName);
        }
    }
    private void storeJsonEvent(String json, String topicName) {
        JsonObject event = JsonParser.parseString(json).getAsJsonObject();
        String ts = event.get("ts").getAsString();
        String ss = event.get("ss").getAsString();
        eventStore.store(topicName, ss, ts, json);
    }

    private void handleConnectionFailure(String topicName, JMSException e) {
        System.err.println("Reconnecting " + topicName + "... " + e.getMessage());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
    private void closeResources(MessageConsumer consumer, Session session, Connection connection) {
        try { if (consumer != null) consumer.close(); } catch (JMSException ignored) {}
        try { if (session != null) session.close(); } catch (JMSException ignored) {}
        try { if (connection != null) connection.close(); } catch (JMSException ignored) {}
    }
}