package es.ulpgc.datos.footballfeeder.control.store;

import com.google.gson.Gson;
import es.ulpgc.datos.footballfeeder.model.Match;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class MatchEventStore implements MatchStore {

    private static final String TOPIC_NAME = "Football";
    private static final String SOURCE_ID = "football-feeder";

    private final Gson gson = new Gson();
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public MatchEventStore(String brokerUrl) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(TOPIC_NAME);
            this.producer = session.createProducer(destination);
        } catch (JMSException e) {
            System.err.println("Error crítico inicializando ActiveMQ: " + e.getMessage());
            throw new RuntimeException("No se pudo establecer conexión con el broker", e);
        }
    }

    public void store(List<Match> matches) {
        if (producer == null || session == null) return;

        try {
            for (Match match : matches) {
                String json = buildEvent(match);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
            }
            System.out.println("Publicados " + matches.size() + " eventos en el topic " + TOPIC_NAME);
        } catch (JMSException e) {
            System.err.println("Error al publicar en ActiveMQ: " + e.getMessage());
        }
    }
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException ignored) {}
    }

    private String buildEvent(Match match) {
        MatchEvent event = new MatchEvent(
                match.getMatchDate().toString() + "Z",
                SOURCE_ID,
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getStatus(),
                match.getCompetition(),
                match.getMatchDate().toString(),
                match.getCity()
        );
        return gson.toJson(event);
    }

    private record MatchEvent(
            String ts,
            String ss,
            String homeTeam,
            String awayTeam,
            int homeScore,
            int awayScore,
            String status,
            String competition,
            String matchDate,
            String city
    ) {}
}