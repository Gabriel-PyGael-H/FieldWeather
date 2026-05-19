package es.ulpgc.datos.footballfeeder.control.store;

import es.ulpgc.datos.footballfeeder.model.Match;
import java.util.List;

public interface MatchStore {

    void store(List<Match> matches);

}