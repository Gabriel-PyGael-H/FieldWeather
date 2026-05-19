package es.ulpgc.datos.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.ulpgc.datos.model.Recommendation;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Datamart {
    private final Connection conn;

    public Datamart(String dbPath) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS match_weather (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                home_team TEXT, away_team TEXT, 
                home_score INTEGER, away_score INTEGER,
                match_date TEXT, city TEXT,
                temperature REAL, humidity INTEGER, description TEXT, prediction_time TEXT,
                recommendation_text TEXT, recommendation_status TEXT, captured_at TEXT
            );
            """;
        try (Statement stmt = conn.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    public synchronized void insertMatchWeather(String home, String away, int hScore, int aScore, String date, String city, Double temp, Integer hum, String desc, String captured) {
        if (date == null || date.length() < 10) return;
        String dayFilter = date.substring(0, 10) + "%";
        String check = "SELECT id FROM match_weather WHERE home_team=? AND away_team=? AND match_date LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, home);
            ps.setString(2, away);
            ps.setString(3, dayFilter);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int existingId = rs.getInt("id");
                String updateScore = "UPDATE match_weather SET home_score=?, away_score=?, match_date=?, city=? WHERE id=?";
                try (PreparedStatement upPs = conn.prepareStatement(updateScore)) {
                    upPs.setInt(1, hScore);
                    upPs.setInt(2, aScore);
                    upPs.setString(3, date);
                    upPs.setString(4, city);
                    upPs.setInt(5, existingId);
                    upPs.executeUpdate();
                }
                return;
            }
        } catch (SQLException e) { e.printStackTrace(); return; }
        String sql = "INSERT INTO match_weather (home_team, away_team, home_score, away_score, match_date, city, temperature, humidity, description, captured_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, home);
            pstmt.setString(2, away);
            pstmt.setInt(3, hScore);
            pstmt.setInt(4, aScore);
            pstmt.setString(5, date);
            pstmt.setString(6, city);
            if (temp != null) pstmt.setDouble(7, temp); else pstmt.setNull(7, Types.REAL);
            if (hum  != null) pstmt.setInt(8, hum);    else pstmt.setNull(8, Types.INTEGER);
            pstmt.setString(9, desc);
            pstmt.setString(10, captured);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public synchronized void updateWeather(String city, double newTemp, int hum, String desc, String newTime) {
        String select = "SELECT match_date, temperature, prediction_time FROM match_weather WHERE city = ? AND match_date LIKE ?";
        String dayFilter = newTime.substring(0, 10) + "%";
        try (PreparedStatement selectStmt = conn.prepareStatement(select)) {
            selectStmt.setString(1, city);
            selectStmt.setString(2, dayFilter);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                String matchDate     = rs.getString("match_date");
                double currentTemp   = rs.getDouble("temperature");
                String currentPred   = rs.getString("prediction_time");
                double finalTemp     = (currentPred != null) ? interpolate(currentTemp, currentPred, newTemp, newTime, matchDate) : newTemp;
                Recommendation rec   = buildRecommendation(finalTemp, desc);
                saveUpdate(city, finalTemp, hum, desc, newTime, rec, dayFilter);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private double interpolate(double t1, String time1, double t2, String time2, String tMatch) {
        try {
            long e1 = parseToEpoch(time1), e2 = parseToEpoch(time2), em = parseToEpoch(tMatch);
            return (e1 == e2) ? t2 : t1 + (t2 - t1) * (double)(em - e1) / (e2 - e1);
        } catch (Exception e) { return t2; }
    }

    private long parseToEpoch(String t) {
        return LocalDateTime.parse(t.replace("Z","").replace("T"," "),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC);
    }

    private Recommendation buildRecommendation(double temp, String desc) {
        String d = desc.toLowerCase();
        boolean isRainy  = d.contains("rain") || d.contains("drizzle") || d.contains("thunderstorm") || d.contains("snow");
        boolean isCloudy = d.contains("clouds") || d.contains("mist") || d.contains("fog");

        if (isRainy) {
            if (temp < 5)  return new Recommendation(" NIEVE Y FRÍO EXTREMO: Riesgo de aplazamiento. Ropa térmica obligatoria.", "CRITICAL");
            if (temp < 15) return new Recommendation(" LLUVIA Y FRÍO: Chubasquero grueso y calzado impermeable. No olvides el paraguas.", "DANGER");
            return new Recommendation(" 🌦️ LLUVIA MODERADA: Hará humedad pero no frío. Un paraguas ligero bastará.", "WARNING");
        }
        if (temp <= 0)  return new Recommendation(" ALERTA POR HELADA: Abrigo de montaña y guantes. El césped estará duro.", "CRITICAL");
        if (temp <= 10) return new Recommendation(" FRÍO INTENSO: Ropa de invierno completa. Ideal para tomar algo caliente en el descanso.", "COLD");
        if (temp <= 16) return new Recommendation(" FRESCO: Una buena chaqueta o sudadera gruesa será necesaria al caer el sol.", "CHILLY");
        if (temp <= 22) {
            if (isCloudy) return new Recommendation(" DÍA GRIS: Temperatura agradable pero sin sol. Una rebeca fina por si refresca.", "INFO");
            return new Recommendation(" TIEMPO PERFECTO: Manga corta o sudadera fina. Disfruta del fútbol.", "PERFECT");
        }
        if (temp <= 28) {
            boolean isClear = d.contains("clear") || d.contains("sun");
            if (isClear) return new Recommendation(" TARDE SOLEADA: Gafas de sol y protección si el estadio no tiene techado.", "SUNNY");
            return new Recommendation(" BOCHORNO: Mucha humedad y calor. Ropa ligera.", "WARM");
        }
        if (temp <= 35) return new Recommendation(" CALOR INTENSO: Hidratación constante. Evita la exposición directa al sol.", "HOT");
        return new Recommendation(" ALERTA POR CALOR: Posibles pausas de hidratación durante el partido. Riesgo de insolación.", "CRITICAL");
    }

    private void saveUpdate(String city, double temp, int hum, String desc, String time, Recommendation rec, String day) throws SQLException {
        String sql = "UPDATE match_weather SET temperature=?, humidity=?, description=?, prediction_time=?, recommendation_text=?, recommendation_status=? WHERE city=? AND match_date LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, temp); pstmt.setInt(2, hum); pstmt.setString(3, desc);
            pstmt.setString(4, time); pstmt.setString(5, rec.getText());
            pstmt.setString(6, rec.getStatus()); pstmt.setString(7, city); pstmt.setString(8, day);
            pstmt.executeUpdate();
        }
    }

    public JsonObject getRecommendation(String team) {
        String sql = "SELECT * FROM match_weather WHERE (home_team=? OR away_team=?) AND match_date >= DATETIME('now') ORDER BY match_date ASC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, team); pstmt.setString(2, team);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JsonObject res = new JsonObject();
                res.addProperty("match",          rs.getString("home_team") + " vs " + rs.getString("away_team"));
                res.addProperty("date",           rs.getString("match_date"));
                res.addProperty("recommendation", rs.getString("recommendation_text"));
                res.addProperty("weather",        rs.getDouble("temperature") + "°C, " + rs.getString("description"));
                return res;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public JsonArray getAllMatches() {
        return fetch("SELECT * FROM match_weather ORDER BY match_date ASC");
    }

    public JsonArray getMatchesByCity(String city) {
        return fetch("SELECT * FROM match_weather WHERE city LIKE ? ORDER BY match_date ASC", "%" + city + "%");
    }

    private JsonArray fetch(String sql, Object... params) {
        JsonArray array = new JsonArray();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) { e.printStackTrace(); }
        return array;
    }
}