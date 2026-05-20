package es.ulpgc.datos.footballfeeder.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private final Match match = new Match(
            "Athletic Club", "Real Betis", 2, 1, "FINISHED",
            "Primera Division", LocalDateTime.of(2026, 3, 22, 20, 0),
            "Bilbao", "2026-05-06T18:00:00Z"
    );

    @Test
    void matchStoresHomeTeamCorrectly() {
        assertEquals("Athletic Club", match.getHomeTeam());
    }

    @Test
    void matchStoresScoreCorrectly() {
        assertEquals(2, match.getHomeScore());
        assertEquals(1, match.getAwayScore());
    }

    @Test
    void matchCapturedAtIsSetAutomatically() {
        assertNotNull(match.getCapturedAt());
    }

    @Test
    void toStringContainsTeamNames() {
        String result = match.toString();
        assertTrue(result.contains("Athletic Club"));
        assertTrue(result.contains("Real Betis"));
    }
}