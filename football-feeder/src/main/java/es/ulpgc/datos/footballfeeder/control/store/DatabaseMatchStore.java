package es.ulpgc.datos.footballfeeder.control.store;

import es.ulpgc.datos.footballfeeder.model.Match;

import java.sql.*;
import java.util.List;

public class DatabaseMatchStore implements MatchStore {

    private final String dbUrl;

    public DatabaseMatchStore(String databaseName) {
        this.dbUrl = "jdbc:sqlite:" + databaseName;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS matches (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    home_team   TEXT NOT NULL,
                    away_team   TEXT NOT NULL,
                    home_score  INTEGER,
                    away_score  INTEGER,
                    status      TEXT,
                    competition TEXT,
                    match_date  TEXT,
                    captured_at TEXT NOT NULL
                );
                """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    @Override
    public void store(List<Match> matches) {
        String checkSql = "SELECT COUNT(*) FROM matches WHERE home_team = ? AND away_team = ? AND match_date = ?";
        String insertSql = "INSERT INTO matches (home_team, away_team, home_score, away_score, status, competition, match_date, captured_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int inserted = 0;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);
            for (Match match : matches) {
                if (!isDuplicate(checkStmt, match)) {
                    executeInsert(insertStmt, match);
                    inserted++;
                }
            }
            conn.commit();

            logResults(matches.size(), inserted);
        } catch (SQLException e) {
            System.err.println("Error al guardar en la base de datos: " + e.getMessage());
        }
    }

    private boolean isDuplicate(PreparedStatement checkStmt, Match match) throws SQLException {
        checkStmt.setString(1, match.getHomeTeam());
        checkStmt.setString(2, match.getAwayTeam());
        checkStmt.setString(3, match.getMatchDate().toString());
        try (ResultSet rs = checkStmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void executeInsert(PreparedStatement insertStmt, Match match) throws SQLException {
        insertStmt.setString(1, match.getHomeTeam());
        insertStmt.setString(2, match.getAwayTeam());
        insertStmt.setInt(3, match.getHomeScore());
        insertStmt.setInt(4, match.getAwayScore());
        insertStmt.setString(5, match.getStatus());
        insertStmt.setString(6, match.getCompetition());
        insertStmt.setString(7, match.getMatchDate().toString());
        insertStmt.setString(8, match.getCapturedAt().toString());
        insertStmt.executeUpdate();
    }

    private void logResults(int total, int inserted) {
        System.out.println("El Feeder ha enviado: " + total + " partidos.");
        System.out.println("Base de datos actualizada: " + inserted + " partidos guardados.");
    }
}