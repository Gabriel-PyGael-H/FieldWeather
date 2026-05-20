package es.ulpgc.datos.weatherfeeder.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherMapperTest {
    private final WeatherMapper mapper = new WeatherMapper();
    private List<WeatherEvent> result;

    @BeforeEach
    void setUp() {
        JsonObject sampleJson = JsonParser.parseString("""
            {
                "city": { "name": "Madrid", "country": "ES" },
                "list": [
                    {
                        "dt_txt": "2024-05-12 21:00:00",
                        "main": { "temp": 20.5, "feels_like": 19.0, "humidity": 60 },
                        "weather": [ { "description": "clear sky" } ]
                    }
                ]
            }
            """).getAsJsonObject();

        result = mapper.map(sampleJson, "Madrid");
    }

    @Test
    void mapsCityCorrectly() {
        assertEquals("Madrid", result.get(0).getCity());
    }

    @Test
    void mapsDateTimeCorrectly() {
        assertNotNull(result.get(0).getPredictionTime());
        assertTrue(result.get(0).getPredictionTime().contains("21:00:00"));
    }

    @Test
    void mapsTemperatureCorrectly() {
        assertEquals(20.5, result.get(0).getTemperature());
    }

    @Test
    void mapsListSize() {
        assertEquals(1, result.size());
    }
}