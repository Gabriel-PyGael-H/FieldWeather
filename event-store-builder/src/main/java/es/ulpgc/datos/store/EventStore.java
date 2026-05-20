package es.ulpgc.datos.store;

import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStore {

    private final String baseDir;
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneOffset.UTC);

    public EventStore() {
        this.baseDir = "eventstore";
    }

    public EventStore(String baseDir) {
        this.baseDir = baseDir;
    }

    public void store(String topic, String ss, String ts, String json) {
        try {
            Instant instant = Instant.parse(ts);
            String date = FILE_NAME_FORMATTER.format(instant);

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