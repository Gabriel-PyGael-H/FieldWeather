package es.ulpgc.datos.businessunit.model;

import es.ulpgc.datos.footballfeeder.model.Match;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match createSampleMatch() {
        return new Match("Athletic Club", "Real Betis", 2, 1,
                "FINISHED", "Primera Division", LocalDateTime.of(2026, 3, 22, 20, 0), "Bilbao", "2026-05-06T18:00:00Z");
    }

    @Test
    void matchStoresHomeTeamCorrectly() {
        assertEquals("Athletic Club", createSampleMatch().getHomeTeam());
    }

    @Test
    void matchStoresScoreCorrectly() {
        Match m = createSampleMatch();
        assertEquals(2, m.getHomeScore());
        assertEquals(1, m.getAwayScore());
    }

    @Test
    void matchCapturedAtIsSetAutomatically() {
        assertNotNull(createSampleMatch().getCapturedAt());
    }

    @Test
    void toStringContainsTeamNames() {
        String result = createSampleMatch().toString();
        assertTrue(result.contains("Athletic Club"));
        assertTrue(result.contains("Real Betis"));
    }
}