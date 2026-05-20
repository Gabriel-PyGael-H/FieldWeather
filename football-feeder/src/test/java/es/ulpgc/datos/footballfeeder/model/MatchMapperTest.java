package es.ulpgc.datos.footballfeeder.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    private final MatchMapper mapper = new MatchMapper();

    private final Match sampleMatch = mapper.map(JsonParser.parseString("""
        {
            "homeTeam": {"name": "Real Madrid CF"},
            "awayTeam": {"name": "FC Barcelona"},
            "score": {
                "fullTime": {"home": 2, "away": 1}
            },
            "status": "FINISHED",
            "competition": {"name": "Primera Division"},
            "utcDate": "2026-03-22T20:00:00Z"
        }
        """).getAsJsonObject());

    @Test
    void mapReturnsCorrectHomeTeam() {
        assertEquals("Real Madrid CF", sampleMatch.getHomeTeam());
    }

    @Test
    void mapReturnsCorrectAwayTeam() {
        assertEquals("FC Barcelona", sampleMatch.getAwayTeam());
    }

    @Test
    void mapReturnsCorrectScore() {
        assertEquals(2, sampleMatch.getHomeScore());
        assertEquals(1, sampleMatch.getAwayScore());
    }

    @Test
    void mapReturnsCorrectStatus() {
        assertEquals("FINISHED", sampleMatch.getStatus());
    }

    @Test
    void mapSetsNullScoreToZero() {
        JsonObject nullScoreJson = JsonParser.parseString("""
            {
                "homeTeam": {"name": "Sevilla FC"},
                "awayTeam": {"name": "Getafe CF"},
                "score": {
                    "fullTime": {"home": null, "away": null}
                },
                "status": "SCHEDULED",
                "competition": {"name": "Primera Division"},
                "utcDate": "2026-04-15T20:00:00Z"
            }
            """).getAsJsonObject();

        Match match = mapper.map(nullScoreJson);
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
    }

    @Test
    void mapSetsCapturedAtNotNull() {
        assertNotNull(sampleMatch.getCapturedAt());
    }

    @Test
    void mapReturnsCorrectCity() {
        assertEquals("Madrid", sampleMatch.getCity());
    }
}