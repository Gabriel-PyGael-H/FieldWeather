package es.ulpgc.datos.weatherfeeder.control.store;

import es.ulpgc.datos.weatherfeeder.model.WeatherEvent;
import java.sql.*;
import java.util.List;

public class DatabaseWeatherStore implements WeatherStore {
    private static final String CREATE_TABLE_SQL = """
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

    private static final String INSERT_WEATHER_SQL =
            "INSERT OR REPLACE INTO weather (city, country, temperature, feels_like, humidity, description, prediction_time, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private final Connection connection;

    public DatabaseWeatherStore(String databaseName) {
        try {
            this.connection = initConnection(databaseName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private Connection initConnection(String databaseName) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
        initializeDatabase(conn);
        return conn;
    }

    private void initializeDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    @Override
    public void store(List<WeatherEvent> weatherEvents) {
        try {
            executeStoreTransaction(weatherEvents);
        } catch (SQLException e) {
            System.err.println("Error saving data to database: " + e.getMessage());
        }
    }

    private void executeStoreTransaction(List<WeatherEvent> weatherEvents) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_WEATHER_SQL)) {
            connection.setAutoCommit(false);
            processBatch(pstmt, weatherEvents);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void processBatch(PreparedStatement pstmt, List<WeatherEvent> weatherEvents) throws SQLException {
        for (WeatherEvent weather : weatherEvents) {
            mapWeatherToStatement(pstmt, weather);
            pstmt.addBatch();
        }
        pstmt.executeBatch();
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