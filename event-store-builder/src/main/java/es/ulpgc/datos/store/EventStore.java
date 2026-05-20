package es.ulpgc.datos.store;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class EventStore {

    private final String baseDir;
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .optionalEnd()
            .appendOffsetId()
            .toFormatter();

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneOffset.UTC);

    public EventStore(String baseDir) {
        this.baseDir = baseDir;
    }

    public void store(String topic, String ss, String ts, String json) {
        try {
            var instant = LocalDateTime.parse(ts, FLEXIBLE_FORMATTER)
                    .toInstant(ZoneOffset.UTC);

            String date = FILE_NAME_FORMATTER.format(instant);

            Path dir = Paths.get(baseDir, topic, ss);
            Files.createDirectories(dir);

            Path file = dir.resolve(date + ".events");

            Files.writeString(file, json + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (Exception e) {
            System.err.println("Error procesando fecha: " + ts + " -> " + e.getMessage());
        }
    }
}