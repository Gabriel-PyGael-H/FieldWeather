package es.ulpgc.datos.datamart;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;
import java.time.LocalDateTime;

public class Datamart {
    private final Connection conn;

    public Datamart(String databasePath) {
        try {
            String url = "jdbc:sqlite:" + databasePath + "?busy_timeout=5000";
            conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
            }
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con SQLite: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS match_weather (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    home_team   TEXT NOT NULL,
                    away_team   TEXT NOT NULL,
                    home_score  INTEGER,
                    away_score  INTEGER,
                    match_date  TEXT,
                    city        TEXT,
                    temperature REAL,
                    humidity    INTEGER,
                    description TEXT,
                    captured_at TEXT,
                    prediction_time TEXT
                );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creando tabla: " + e.getMessage());
        }
    }

    public synchronized void insertMatchWeather(String homeTeam, String awayTeam, int homeScore, int awayScore,
                                                String matchDate, String city, Double temperature, Integer humidity,
                                                String description, String capturedAt) {
        String checkSql = "SELECT COUNT(*) FROM match_weather WHERE home_team = ? AND away_team = ? AND match_date = ?";
        try {
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, homeTeam);
                check.setString(2, awayTeam);
                check.setString(3, matchDate);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return;
                }
            }
            String insertSql = "INSERT INTO match_weather (home_team, away_team, home_score, away_score, match_date, city, temperature, humidity, description, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, homeTeam);
                pstmt.setString(2, awayTeam);
                pstmt.setInt(3, homeScore);
                pstmt.setInt(4, awayScore);
                pstmt.setString(5, matchDate);
                pstmt.setString(6, city);
                if (temperature != null) pstmt.setDouble(7, temperature); else pstmt.setNull(7, Types.REAL);
                if (humidity != null) pstmt.setInt(8, humidity); else pstmt.setNull(8, Types.INTEGER);
                pstmt.setString(9, description != null ? description : "Historical data");
                pstmt.setString(10, capturedAt);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) { System.err.println("Error insertando: " + e.getMessage()); }
    }

    public synchronized void updateWeather(String city, double temperature, int humidity, String description, String predictionTime) {
        String sql = "UPDATE match_weather SET temperature = ?, humidity = ?, description = ?, prediction_time = ? " +
                "WHERE city = ? AND match_date LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, temperature);
            pstmt.setInt(2, humidity);
            pstmt.setString(3, description);
            pstmt.setString(4, predictionTime);
            pstmt.setString(5, city);
            pstmt.setString(6, predictionTime.substring(0, 10) + "%");
            pstmt.executeUpdate();
        } catch (SQLException e) { System.err.println("Error actualizando: " + e.getMessage()); }
    }

    public JsonArray getAllMatches() {
        return fetchAsJson("SELECT * FROM match_weather ORDER BY match_date ASC");
    }

    public JsonArray getMatchesByCity(String city) {
        return fetchAsJson("SELECT * FROM match_weather WHERE city = ? ORDER BY match_date ASC", city);
    }

    public JsonArray getMatchesByTeam(String team) {
        return fetchAsJson("SELECT * FROM match_weather WHERE home_team = ? OR away_team = ? ORDER BY match_date ASC", team, team);
    }

    public JsonArray getRainyMatches() {
        return fetchAsJson("SELECT * FROM match_weather WHERE description LIKE '%rain%' OR description LIKE '%drizzle%'");
    }
    public JsonObject getRecommendation(String team) {
        String now = LocalDateTime.now().toString();
        String sql = "SELECT * FROM match_weather WHERE (home_team = ? OR away_team = ?) AND match_date >= ? ORDER BY match_date ASC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, team);
            pstmt.setString(2, team);
            pstmt.setString(3, now);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JsonObject res = new JsonObject();
                double t = rs.getDouble("temperature");
                String d = rs.getString("description").toLowerCase();

                res.addProperty("team", team);
                res.addProperty("match", rs.getString("home_team") + " vs " + rs.getString("away_team"));
                res.addProperty("city", rs.getString("city"));
                res.addProperty("date", rs.getString("match_date"));
                res.addProperty("weather", t + "°C, " + d);

                String rec = "¡Disfruta del partido!";
                if (d.contains("rain") || d.contains("drizzle")) rec = "¡Lleva paraguas! Se espera lluvia.";
                else if (t < 15 && t > 0) rec = "¡Abrígate! Hará frío en el estadio.";

                res.addProperty("recommendation", rec);
                return res;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private JsonArray fetchAsJson(String sql, Object... params) {
        JsonArray array = new JsonArray();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) pstmt.setObject(i + 1, params[i]);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("homeTeam", rs.getString("home_team"));
                obj.addProperty("awayTeam", rs.getString("away_team"));
                obj.addProperty("homeScore", rs.getInt("home_score"));
                obj.addProperty("awayScore", rs.getInt("away_score"));
                obj.addProperty("matchDate", rs.getString("match_date"));
                obj.addProperty("city", rs.getString("city"));
                obj.addProperty("temperature", rs.getDouble("temperature"));
                obj.addProperty("humidity", rs.getInt("humidity"));
                obj.addProperty("description", rs.getString("description"));
                array.add(obj);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return array;
    }
}