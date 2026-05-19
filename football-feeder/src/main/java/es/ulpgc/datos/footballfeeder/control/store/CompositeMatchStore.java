package es.ulpgc.datos.footballfeeder.control.store;

import es.ulpgc.datos.footballfeeder.model.Match;
import java.util.List;

public class CompositeMatchStore implements MatchStore {

    private final MatchStore dbStore;
    private final MatchStore eventStore;

    public CompositeMatchStore(MatchStore dbStore, MatchStore eventStore) {
        this.dbStore = dbStore;
        this.eventStore = eventStore;
    }

    @Override
    public void store(List<Match> matches) {
        dbStore.store(matches);
        eventStore.store(matches);
    }
}