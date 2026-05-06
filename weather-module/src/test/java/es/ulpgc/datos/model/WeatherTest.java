package es.ulpgc.datos.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class WeatherTest {
    private Weather createSampleWeather() {
        LocalDateTime testDate = LocalDateTime.of(2024, 5, 12, 21, 0);
        return new Weather("Madrid", 10.79, 9.5, 45, "clear sky", "ES", testDate, "2026-05-06T21:00:00Z");
    }

    @Test
    void weatherStoresCityCorrectly() {
        assertEquals("Madrid", createSampleWeather().getCity());
    }

    @Test
    void weatherStoresTemperatureCorrectly() {
        assertEquals(10.79, createSampleWeather().getTemperature());
    }

    @Test
    void weatherStoresHumidityCorrectly() {
        assertEquals(45, createSampleWeather().getHumidity());
    }

    @Test
    void weatherPredictionTimeIsSetCorrectly() {
        LocalDateTime expectedDate = LocalDateTime.of(2024, 5, 12, 21, 0);
        assertEquals(expectedDate, createSampleWeather().getPredictionTime());
    }

    @Test
    void toStringContainsCityName() {
        assertTrue(createSampleWeather().toString().contains("Madrid"));
    }
}