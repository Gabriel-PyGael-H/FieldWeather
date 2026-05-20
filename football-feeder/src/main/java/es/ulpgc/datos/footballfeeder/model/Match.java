package es.ulpgc.datos.footballfeeder.model;

import java.time.LocalDateTime;

public class Match {

    private final String homeTeam;
    private final String awayTeam;
    private final int homeScore;
    private final int awayScore;
    private final String status;
    private final String competition;
    private final LocalDateTime matchDate;
    private final String capturedAt;
    private final String city;

    public Match(String homeTeam, String awayTeam, int homeScore, int awayScore,
                 String status, String competition, LocalDateTime matchDate, String capturedAt, String city) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.status = status;
        this.competition = competition;
        this.matchDate = matchDate;
        this.capturedAt = capturedAt;
        this.city = city;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public String getStatus() {
        return status;
    }

    public String getCompetition() {
        return competition;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public String getCapturedAt() {
        return capturedAt;
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayScore + " " + awayTeam
                + " [" + status + "] (" + city + ") " + matchDate + " | Captured UTC: " + capturedAt;
    }
}