package es.ulpgc.datos.businessunit.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.ulpgc.datos.businessunit.model.Recommendation;
import java.sql.*;

public class Datamart {
    private final Connection connection;

    public Datamart(String dbPath) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS match_weather (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            home_team TEXT NOT NULL,
            away_team TEXT NOT NULL,
            home_score INTEGER DEFAULT 0,
            away_score INTEGER DEFAULT 0,
            match_date TEXT NOT NULL,
            city TEXT,
            temperature REAL,
            humidity INTEGER,
            description TEXT,
            prediction_time TEXT,
            recommendation_text TEXT,
            recommendation_status TEXT,
            captured_at TEXT
        );
        """;
        execute(sql);
    }

    public synchronized void insertMatchWeather(String home, String away, int hScore, int aScore, String date, String city, Double temp, Integer hum, String desc, String captured) {
        if (date == null || date.length() < 10) return;
        Integer existingId = findMatchId(home, away, date.substring(0, 10) + "%");

        if (existingId != null) {
            updateMatchScore(existingId, hScore, aScore, date, city);
        } else {
            insertNewMatch(home, away, hScore, aScore, date, city, temp, hum, desc, captured);
        }
    }

    public JsonObject getMatchDataForInterpolation(String city, String dayFilter) {
        String sql = "SELECT match_date, temperature, prediction_time FROM match_weather WHERE city = ? AND match_date LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, city);
            pstmt.setString(2, dayFilter);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JsonObject data = new JsonObject();
                data.addProperty("matchDate", rs.getString("match_date"));
                data.addProperty("temperature", rs.getDouble("temperature"));
                data.addProperty("predictionTime", rs.getString("prediction_time"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void updateWeather(String city, double temp, int hum, String desc, String time, Recommendation rec, String day) {
        String sql = "UPDATE match_weather SET temperature=?, humidity=?, description=?, prediction_time=?, recommendation_text=?, recommendation_status=? WHERE city=? AND match_date LIKE ?";
        execute(sql, temp, hum, desc, time, rec.getText(), rec.getStatus(), city, day);
    }

    public JsonObject getRecommendation(String team) {
        String sql = "SELECT * FROM match_weather WHERE (home_team=? OR away_team=?) AND match_date >= DATETIME('now') ORDER BY match_date ASC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, team);
            pstmt.setString(2, team);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JsonObject res = new JsonObject();
                res.addProperty("match",          rs.getString("home_team") + " vs " + rs.getString("away_team"));
                res.addProperty("date",           rs.getString("match_date"));
                res.addProperty("recommendation", rs.getString("recommendation_text"));
                res.addProperty("weather",        rs.getDouble("temperature") + "°C, " + rs.getString("description"));
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JsonArray getAllMatches() {
        return fetch("SELECT * FROM match_weather ORDER BY match_date ASC");
    }

    public JsonArray getMatchesByCity(String city) {
        return fetch("SELECT * FROM match_weather WHERE city LIKE ? ORDER BY match_date ASC", "%" + city + "%");
    }

    private Integer findMatchId(String home, String away, String dayFilter) {
        String sql = "SELECT id FROM match_weather WHERE home_team=? AND away_team=? AND match_date LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, home);
            ps.setString(2, away);
            ps.setString(3, dayFilter);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        } catch (SQLException e) {
            return null;
        }
    }

    private void updateMatchScore(int id, int h, int a, String date, String city) {
        String sql = "UPDATE match_weather SET home_score=?, away_score=?, match_date=?, city=? WHERE id=?";
        execute(sql, h, a, date, city, id);
    }

    private void insertNewMatch(String home, String away, int h, int a, String date, String city, Double temp, Integer hum, String desc, String cap) {
        String sql = "INSERT INTO match_weather (home_team, away_team, home_score, away_score, match_date, city, temperature, humidity, description, captured_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        execute(sql, home, away, h, a, date, city, temp, hum, desc, cap);
    }

    private void execute(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JsonArray fetch(String sql, Object... params) {
        JsonArray array = new JsonArray();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) pstmt.setObject(i + 1, params[i]);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JsonObject o = new JsonObject();
                o.addProperty("homeTeam",       rs.getString("home_team"));
                o.addProperty("awayTeam",       rs.getString("away_team"));
                o.addProperty("homeScore",      rs.getInt("home_score"));
                o.addProperty("awayScore",      rs.getInt("away_score"));
                o.addProperty("matchDate",      rs.getString("match_date"));
                o.addProperty("city",           rs.getString("city"));
                o.addProperty("temperature",    rs.getObject("temperature") != null ? rs.getDouble("temperature") : null);
                o.addProperty("description",    rs.getString("description"));
                o.addProperty("recommendation", rs.getString("recommendation_text"));
                array.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }
}