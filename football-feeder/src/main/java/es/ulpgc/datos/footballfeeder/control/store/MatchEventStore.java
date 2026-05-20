package es.ulpgc.datos.footballfeeder.control.store;

import com.google.gson.Gson;
import es.ulpgc.datos.footballfeeder.model.Match;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class MatchEventStore implements MatchStore, AutoCloseable {

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
            System.err.println("Error al inicializar ActiveMQ en MatchEventStore: " + e.getMessage());
        }
    }

    @Override
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

    private String buildEvent(Match match) {
        return gson.toJson(new MatchEvent(
                match.getCapturedAt(),
                SOURCE_ID,
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getStatus(),
                match.getCompetition(),
                match.getMatchDate().toString(),
                match.getCity()
        ));
    }

    @Override
    public void close() {
        try { if (producer != null) producer.close(); } catch (JMSException ignored) {}
        try { if (session != null) session.close(); } catch (JMSException ignored) {}
        try { if (connection != null) connection.close(); } catch (JMSException ignored) {}
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