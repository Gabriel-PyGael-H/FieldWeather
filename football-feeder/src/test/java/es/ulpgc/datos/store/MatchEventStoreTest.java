package es.ulpgc.datos.store;

import es.ulpgc.datos.footballfeeder.control.store.MatchEventStore;
import es.ulpgc.datos.footballfeeder.model.Match;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchEventStoreTest {

    @Test
    void publishDoesNotThrowWithEmptyList() {
        MatchEventStore publisher = new MatchEventStore("tcp://localhost:61616");
        assertDoesNotThrow(() -> publisher.store(List.of()));
    }

    @Test
    void publishDoesNotThrowWithValidMatches() {
        MatchEventStore publisher = new MatchEventStore("tcp://localhost:61616");
        Match match = new Match("Real Madrid CF", "FC Barcelona", 3, 2,
                "FINISHED", "Primera Division", LocalDateTime.of(2026, 3, 22, 20, 0), "Madrid", "2026-05-06T18:00:00Z");

        assertDoesNotThrow(() -> publisher.store(List.of(match)));
    }
}