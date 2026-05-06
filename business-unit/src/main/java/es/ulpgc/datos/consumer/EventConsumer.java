package es.ulpgc.datos.consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.datamart.Datamart;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class EventConsumer {
    private static final String CLIENT_ID = "business-unit-static-v1";
    private final String brokerUrl;
    private final Datamart datamart;

    public EventConsumer(String brokerUrl, Datamart datamart) {
        this.brokerUrl = "failover:(" + brokerUrl + ")?maxReconnectAttempts=10";
        this.datamart = datamart;
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

                    System.out.println("Subscribed to topic: " + topicName);

                    while (true) {
                        Message message = consumer.receive();
                        if (message instanceof TextMessage textMessage) {
                            try {
                                JsonObject event = JsonParser.parseString(textMessage.getText()).getAsJsonObject();
                                processEvent(topicName, event);
                            } catch (Exception e) {
                                System.err.println("Error processing message: " + e.getMessage());
                            }
                        }
                    }
                } catch (JMSException e) {
                    System.err.println("JMS Error in " + topicName + ": " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                }
            }
        }).start();
    }

    private void processEvent(String topic, JsonObject event) {
        if (topic.equals("Football")) {
            String home = event.get("homeTeam").getAsString();
            // matchDate is used as the primary time reference
            String matchDate = event.get("matchDate").getAsString();

            datamart.insertMatchWeather(
                    home,
                    event.get("awayTeam").getAsString(),
                    event.get("homeScore").getAsInt(),
                    event.get("awayScore").getAsInt(),
                    matchDate,
                    getCityForTeam(home),
                    null,
                    null,
                    "No weather data available for this date yet",
                    event.get("ts").getAsString()
            );
        } else if (topic.equals("Weather")) {
            // We use predictionTime to match the weather event to the correct match slot
            String predictionTime = event.get("predictionTime").getAsString();

            datamart.updateWeather(
                    event.get("city").getAsString(),
                    event.get("temperature").getAsDouble(),
                    event.get("humidity").getAsInt(),
                    event.get("description").getAsString(),
                    predictionTime
            );
        }
    }

    private String getCityForTeam(String team) {
        return switch (team) {
            case "Real Madrid CF", "Club Atlético de Madrid", "Getafe CF", "Rayo Vallecano de Madrid" -> "Madrid";
            case "FC Barcelona", "RCD Espanyol de Barcelona" -> "Barcelona";
            case "Sevilla FC", "Real Betis Balompié" -> "Sevilla"; // Using the Spanish name for consistency
            case "Valencia CF", "Levante UD" -> "Valencia";
            case "Athletic Club" -> "Bilbao";
            case "Girona FC" -> "Girona";
            case "CA Osasuna" -> "Pamplona";
            case "RCD Mallorca" -> "Palma de Mallorca";
            case "Real Sociedad de Fútbol" -> "San Sebastian";
            case "Villarreal CF" -> "Villarreal";
            case "RC Celta de Vigo" -> "Vigo";
            case "Deportivo Alavés" -> "Vitoria";
            case "Elche CF" -> "Elche";
            case "Real Oviedo" -> "Oviedo";
            default -> "Unknown";
        };
    }
}