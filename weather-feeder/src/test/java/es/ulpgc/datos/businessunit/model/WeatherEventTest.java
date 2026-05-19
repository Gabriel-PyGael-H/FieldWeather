package es.ulpgc.datos.businessunit.model;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherEventTest {

    private WeatherEvent createSampleEvent() {
        return new WeatherEvent(
                "2026-05-06T21:00:00Z",
                "weather-feeder-v1",
                "Madrid",
                "ES",
                10.79,
                9.5,
                45,
                "clear sky",
                "2024-05-12 21:00:00"
        );
    }

    @Test
    void eventStoresCityCorrectly() {
        assertEquals("Madrid", createSampleEvent().getCity());
    }

    @Test
    void eventStoresTemperatureCorrectly() {
        assertEquals(10.79, createSampleEvent().getTemperature());
    }

    @Test
    void eventStoresSourceSystem() {
        assertEquals("weather-feeder-v1", createSampleEvent().getSs());
    }

    @Test
    void eventStoresPredictionTimeCorrectly() {
        assertEquals("2024-05-12 21:00:00", createSampleEvent().getPredictionTime());
    }

    @Test
    void eventStoresHumidity() {
        assertEquals(45, createSampleEvent().getHumidity());
    }
}