package es.ulpgc.datos.footballfeeder.model;

import java.time.LocalDateTime;

public class Match {

    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    private String status;
    private String competition;
    private LocalDateTime matchDate;
    private LocalDateTime capturedAt;
    private String city;

    public Match(String homeTeam, String awayTeam, int homeScore, int awayScore,
                 String status, String competition, LocalDateTime matchDate, String city) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.status = status;
        this.competition = competition;
        this.matchDate = matchDate;
        this.city = city;
        this.capturedAt = LocalDateTime.now();
    }

    public String getHomeTeam()        { return homeTeam; }
    public String getAwayTeam()        { return awayTeam; }
    public int getHomeScore()          { return homeScore; }
    public int getAwayScore()          { return awayScore; }
    public String getStatus()          { return status; }
    public String getCompetition()     { return competition; }
    public LocalDateTime getMatchDate()   { return matchDate; }
    public LocalDateTime getCapturedAt()  { return capturedAt; }
    public String getCity() { return city; }

    @Override
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayScore + " " + awayTeam
                + " [" + status + "] " + matchDate;
    }
}