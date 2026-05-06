package es.ulpgc.datos.store;

import es.ulpgc.datos.model.Weather;

import java.sql.*;
import java.util.List;

public class DatabaseWeatherStore implements WeatherStore {

    private final String dbUrl;

    public DatabaseWeatherStore(String databaseName) {
        this.dbUrl = "jdbc:sqlite:" + databaseName;
        createTableIfNotExists();
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
                    PRIMARY KEY (city, prediction_time)
                );
                """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla weather: " + e.getMessage());
        }
    }

    @Override
    public void store(List<Weather> weatherList) {
        String sql = """
                INSERT OR REPLACE INTO weather
                    (city, country, temperature, feels_like, humidity, description, prediction_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Weather weather : weatherList) {
                pstmt.setString(1, weather.getCity());
                pstmt.setString(2, weather.getCountry());
                pstmt.setDouble(3, weather.getTemperature());
                pstmt.setDouble(4, weather.getFeelsLike());
                pstmt.setInt(5, weather.getHumidity());
                pstmt.setString(6, weather.getDescription());
                pstmt.setString(7, weather.getPredictionTime().toString());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            System.out.println("Sincronizados " + weatherList.size() + " registros climáticos.");

        } catch (SQLException e) {
            System.err.println("Error al guardar en la base de datos: " + e.getMessage());
        }
    }
}