package es.ulpgc.datos.footballfeeder.control.feeder;

import es.ulpgc.datos.footballfeeder.model.Match;
import java.util.List;

public interface FootballFeeder {

    List<Match> fetchMatches();

}