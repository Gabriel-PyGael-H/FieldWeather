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
    private final ScheduledExecutorService scheduler;

    public Controller(FootballFeeder feeder, MatchStore store) {
        this.feeder = feeder;
        this.store = store;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::fetchSafely, 0, 1, TimeUnit.HOURS);
        System.out.println("Scheduler iniciado. Capturando datos cada hora...");
    }

    private void fetchSafely() {
        try {
            System.out.println("Obteniendo partidos de LaLiga...");
            List<Match> matches = feeder.fetchMatches();
            System.out.println("Partidos obtenidos: " + matches.size());

            matches.forEach(System.out::println);
            store.store(matches);
        } catch (Exception e) {
            System.err.println("Error crítico inesperado en el ciclo de captura: " + e.getMessage());
        }
    }
}