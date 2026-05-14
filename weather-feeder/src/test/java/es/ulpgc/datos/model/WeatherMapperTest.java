package es.ulpgc.datos.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import es.ulpgc.datos.weatherfeeder.model.WeatherMapper;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherMapperTest {

    private final WeatherMapper mapper = new WeatherMapper();

    private JsonObject sampleJson() {
        String json = """
            {
                "city": { "name": "Madrid", "country": "ES" },
                "list": [
                    {
                        "dt_txt": "2024-05-12 21:00:00",
                        "main": {
                            "temp": 20.5,
                            "feels_like": 19.0,
                            "humidity": 60
                        },
                        "weather": [
                            { "description": "clear sky" }
                        ]
                    }
                ]
            }
            """;
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void mapsCityCorrectly() {
        List<WeatherEvent> result = mapper.map(sampleJson());
        assertEquals("Madrid", result.get(0).getCity());
    }

    @Test
    void mapsDateTimeCorrectly() {
        List<WeatherEvent> result = mapper.map(sampleJson());
        assertNotNull(result.get(0).getPredictionTime());
        assertTrue(result.get(0).getPredictionTime().contains("21:00:00"));
    }

    @Test
    void mapsTemperatureCorrectly() {
        List<WeatherEvent> result = mapper.map(sampleJson());
        assertEquals(20.5, result.get(0).getTemperature());
    }

    @Test
    void mapsListSize() {
        List<WeatherEvent> result = mapper.map(sampleJson());
        assertEquals(1, result.size());
    }
}