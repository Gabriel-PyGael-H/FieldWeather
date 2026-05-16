package es.ulpgc.datos.control;

import es.ulpgc.datos.model.Datamart;
import es.ulpgc.datos.view.RestApi;

public class Controller {
    private final Datamart datamart;
    private final RestApi restApi;

    public Controller(Datamart datamart) {
        this.datamart = datamart;
        this.restApi = new RestApi(datamart);
    }

    public void start(int port) {
        restApi.start(port);

        System.out.println("Complejos del sistema inicializados correctamente.");
    }
}