package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import java.sql.*;
import java.util.List;

public class DatabaseWeatherStore implements WeatherStore {
    private final Connection connection;

    public DatabaseWeatherStore(String databaseName) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS weather (
                    city            TEXT NOT NULL,
                    country         TEXT,
                    temperature     REAL,
                    feels_like      REAL,
                    humidity        INTEGER,
                    description     TEXT,
                    prediction_time TEXT NOT NULL,
                    captured_at     TEXT,
                    PRIMARY KEY (city, prediction_time)
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla weather: " + e.getMessage());
        }
    }

    @Override
    public void store(List<WeatherEvent> weatherEvents) {
        String sql = "INSERT OR REPLACE INTO weather (city, country, temperature, feels_like, humidity, description, prediction_time, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (WeatherEvent weather : weatherEvents) {
                mapWeatherToStatement(pstmt, weather);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            System.err.println("Error al guardar en la base de datos: " + e.getMessage());
        }
    }

    private void mapWeatherToStatement(PreparedStatement pstmt, WeatherEvent weather) throws SQLException {
        pstmt.setString(1, weather.getCity());
        pstmt.setString(2, weather.getCountry());
        pstmt.setDouble(3, weather.getTemperature());
        pstmt.setDouble(4, weather.getFeelsLike());
        pstmt.setInt(5, weather.getHumidity());
        pstmt.setString(6, weather.getDescription());
        pstmt.setString(7, weather.getPredictionTime());
        pstmt.setString(8, weather.getTs());
    }
}