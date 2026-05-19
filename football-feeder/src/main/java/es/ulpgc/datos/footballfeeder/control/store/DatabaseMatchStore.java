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
        String checkSql = """
                SELECT COUNT(*) FROM matches
                WHERE home_team = ? AND away_team = ? AND match_date = ?
                """;
        String insertSql = """
                INSERT INTO matches
                    (home_team, away_team, home_score, away_score, status, competition, match_date, captured_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        int inserted = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            for (Match match : matches) {
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    check.setString(1, match.getHomeTeam());
                    check.setString(2, match.getAwayTeam());
                    check.setString(3, match.getMatchDate().toString());
                    ResultSet rs = check.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                            insert.setString(1, match.getHomeTeam());
                            insert.setString(2, match.getAwayTeam());
                            insert.setInt(3, match.getHomeScore());
                            insert.setInt(4, match.getAwayScore());
                            insert.setString(5, match.getStatus());
                            insert.setString(6, match.getCompetition());
                            insert.setString(7, match.getMatchDate().toString());
                            insert.setString(8, match.getCapturedAt().toString());
                            insert.executeUpdate();
                            inserted++;
                        }
                    }
                }
            }
            System.out.println("El Feeder ha enviado: " + matches.size() + " partidos.");
            System.out.println("Base de datos actualizada: " + inserted + " partidos guardados.");
        } catch (SQLException e) {
            System.err.println("Error al guardar en la base de datos: " + e.getMessage());
        }
    }
}