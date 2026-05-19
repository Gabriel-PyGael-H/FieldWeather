package es.ulpgc.datos.businessunit.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.footballfeeder.model.Match;
import es.ulpgc.datos.footballfeeder.model.MatchMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    private final MatchMapper mapper = new MatchMapper();

    private JsonObject sampleMatchJson() {
        String json = """
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
        """;
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void mapReturnsCorrectHomeTeam() {
        Match match = mapper.map(sampleMatchJson());
        assertEquals("Real Madrid CF", match.getHomeTeam());
    }

    @Test
    void mapReturnsCorrectAwayTeam() {
        Match match = mapper.map(sampleMatchJson());
        assertEquals("FC Barcelona", match.getAwayTeam());
    }

    @Test
    void mapReturnsCorrectScore() {
        Match match = mapper.map(sampleMatchJson());
        assertEquals(2, match.getHomeScore());
        assertEquals(1, match.getAwayScore());
    }

    @Test
    void mapReturnsCorrectStatus() {
        Match match = mapper.map(sampleMatchJson());
        assertEquals("FINISHED", match.getStatus());
    }

    @Test
    void mapSetsNullScoreToZero() {
        String json = """
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
        """;
        Match match = mapper.map(JsonParser.parseString(json).getAsJsonObject());
        assertEquals(0, match.getHomeScore());
        assertEquals(0, match.getAwayScore());
    }

    @Test
    void mapSetsCapturedAtNotNull() {
        Match match = mapper.map(sampleMatchJson());
        assertNotNull(match.getCapturedAt());
    }

    @Test
    void mapReturnsCorrectCity() {
        Match match = mapper.map(sampleMatchJson());
        assertEquals("Madrid", match.getCity());
    }
}