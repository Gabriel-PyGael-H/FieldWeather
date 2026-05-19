package es.ulpgc.datos.footballfeeder.control;
import es.ulpgc.datos.footballfeeder.control.feeder.FootballFeeder;
import es.ulpgc.datos.footballfeeder.model.Match;
import es.ulpgc.datos.footballfeeder.control.store.MatchStore;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    private final FootballFeeder feeder;
    private final MatchStore store;

    public Controller(FootballFeeder feeder, MatchStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::fetch, 0, 1, TimeUnit.HOURS);
        System.out.println("Scheduler iniciado. Capturando datos cada hora...");
    }

    private void fetch() {
        System.out.println("Obteniendo partidos de LaLiga...");
        List<Match> matches = feeder.fetchMatches();
        System.out.println("Partidos obtenidos: " + matches.size());
        matches.forEach(System.out::println);
        store.store(matches);
    }
}