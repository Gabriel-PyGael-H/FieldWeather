package es.ulpgc.datos.businessunit.control;

import es.ulpgc.datos.businessunit.view.UIService;

public class Controller {
    private final Datamart datamart;
    private final UIService UIService;

    public Controller(Datamart datamart) {
        this.datamart = datamart;
        this.UIService = new UIService(datamart);
    }

    public void start(int port) {
        UIService.start(port);

        System.out.println("Complejos del sistema inicializados correctamente.");
    }
}