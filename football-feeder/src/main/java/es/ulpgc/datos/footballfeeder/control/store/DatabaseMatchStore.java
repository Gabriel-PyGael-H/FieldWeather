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
                    home_team   TEXT NOT NULL,
                    away_team   TEXT NOT NULL,
                    home_score  INTEGER,
                    away_score  INTEGER,
                    status      TEXT,
                    competition TEXT,
                    match_date  TEXT NOT NULL,
                    captured_at TEXT NOT NULL,
                    city        TEXT,
                    PRIMARY KEY (home_team, away_team, match_date)
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
        String sql = """
                INSERT OR REPLACE INTO matches 
                (home_team, away_team, home_score, away_score, status, competition, match_date, captured_at, city) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (Match match : matches) {
                mapMatchToStatement(pstmt, match);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();

            System.out.println("Base de datos de partidos actualizada con " + matches.size() + " registros.");
        } catch (SQLException e) {
            System.err.println("Error al guardar en la base de datos: " + e.getMessage());
        }
    }

    private void mapMatchToStatement(PreparedStatement pstmt, Match match) throws SQLException {
        pstmt.setString(1, match.getHomeTeam());
        pstmt.setString(2, match.getAwayTeam());
        pstmt.setInt(3, match.getHomeScore());
        pstmt.setInt(4, match.getAwayScore());
        pstmt.setString(5, match.getStatus());
        pstmt.setString(6, match.getCompetition());
        pstmt.setString(7, match.getMatchDate().toString());
        pstmt.setString(8, match.getCapturedAt());
        pstmt.setString(9, match.getCity());
    }
}