package es.ulpgc.datos.weatherfeeder.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherEventTest {

    private final WeatherEvent event = new WeatherEvent(
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

    @Test
    void eventStoresCityCorrectly() {
        assertEquals("Madrid", event.getCity());
    }

    @Test
    void eventStoresTemperatureCorrectly() {
        assertEquals(10.79, event.getTemperature());
    }

    @Test
    void eventStoresSourceSystem() {
        assertEquals("weather-feeder-v1", event.getSs());
    }

    @Test
    void eventStoresPredictionTimeCorrectly() {
        assertEquals("2024-05-12 21:00:00", event.getPredictionTime());
    }

    @Test
    void eventStoresHumidity() {
        assertEquals(45, event.getHumidity());
    }
}