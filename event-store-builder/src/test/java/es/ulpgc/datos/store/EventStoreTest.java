package es.ulpgc.datos.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void storeCreatesCorrectDirectory() throws IOException {
        EventStore store = new EventStore(tempDir.toString());
        store.store("Football", "football-feeder", "2026-03-22T20:00Z", "{\"ts\":\"2026-03-22T20:00Z\",\"ss\":\"football-feeder\"}");

        Path expectedDir = tempDir.resolve("Football/football-feeder");
        assertTrue(Files.exists(expectedDir));
    }

    @Test
    void storeCreatesCorrectFile() throws IOException {
        EventStore store = new EventStore(tempDir.toString());
        store.store("Football", "football-feeder", "2026-03-22T20:00Z", "{\"ts\":\"2026-03-22T20:00Z\",\"ss\":\"football-feeder\"}");

        Path expectedFile = tempDir.resolve("Football/football-feeder/20260322.events");
        assertTrue(Files.exists(expectedFile));
    }

    @Test
    void storeAppendsMultipleEvents() throws IOException {
        EventStore store = new EventStore(tempDir.toString());
        store.store("Football", "football-feeder", "2026-03-22T20:00Z", "{\"event\":\"1\"}");
        store.store("Football", "football-feeder", "2026-03-22T20:00Z", "{\"event\":\"2\"}");

        Path file = tempDir.resolve("Football/football-feeder/20260322.events");
        long lines = Files.lines(file).count();
        assertEquals(2, lines);
    }

    @Test
    void storeDifferentDatesCreatesDifferentFiles() throws IOException {
        EventStore store = new EventStore(tempDir.toString());
        store.store("Football", "football-feeder", "2026-03-22T20:00Z", "{\"event\":\"1\"}");
        store.store("Football", "football-feeder", "2026-03-23T20:00Z", "{\"event\":\"2\"}");

        assertTrue(Files.exists(tempDir.resolve("Football/football-feeder/20260322.events")));
        assertTrue(Files.exists(tempDir.resolve("Football/football-feeder/20260323.events")));
    }
}