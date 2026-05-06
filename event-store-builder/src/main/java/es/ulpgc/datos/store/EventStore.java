package es.ulpgc.datos.store;

import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class EventStore {

    private final String baseDir;
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ISO_INSTANT;

    public EventStore() {
        this.baseDir = "eventstore";
    }

    public EventStore(String baseDir) {
        this.baseDir = baseDir;
    }

    public void store(String topic, String ss, String ts, String json) {
        try {
            java.time.OffsetDateTime dateTime = java.time.OffsetDateTime.parse(ts);

            String date = OUTPUT.format(dateTime);

            Path dir = Paths.get(baseDir, topic, ss);
            Files.createDirectories(dir);

            Path file = dir.resolve(date + ".events");

            Files.writeString(file, json + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (Exception e) {
            System.err.println("Error storing EventStore: " + e.getMessage() + " for ts: " + ts);
        }
    }
}