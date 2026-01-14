package aeron.main;

import aeron.concurrent.ControlTowerConcurrent;
import aeron.model.Airplane;

public class Simulation {

    // CAMBIAMOS ESTO PARA PROBAR UN MODO U OTRO
    // El enunciado pide que se pueda elegir aquí
    private static final SimulationMode MODE = SimulationMode.SEQUENTIAL;

    public static void main(String[] args) {
        if (MODE == SimulationMode.SEQUENTIAL) {
            runSequential();
        } else {
            runConcurrent();
        }
    }

    private static void runConcurrent() {
        System.out.println("--- INICIANDO MODO CONCURRENTE ---");

        // Configuramos de parámetros
        int numAviones = 10;
        int numPistas = 1;
        int numPuertas = 3;
        int numOperarios = 0; // De momento 0

        // 2. INICIAMOS EL LOGGER
        aeron.util.Logger.setup("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);

        // 3. Creamos la Torre
        aeron.concurrent.ControlTowerConcurrent tower = new aeron.concurrent.ControlTowerConcurrent();
        // Nota: Si tu constructor admite parámetros, pásale numPistas y numPuertas

        // 4. ABRIMOS LA VENTANA (¡Esto también faltaba!)
        // Asegúrate de importar aeron.util.AirportWindow
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        aeron.util.Logger.setWindow(ventana);

        // 5. Creamos y lanzamos los aviones
        for (int i = 1; i <= numAviones; i++) {
            String flightId = "IB-" + String.format("%02d", i);
            aeron.model.Airplane avion = new aeron.model.Airplane(flightId, tower);
            new Thread(avion).start();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    private static void runSequential() {
        System.out.println("--- INICIANDO MODO SECUENCIAL ---");

        int numAviones = 10;
        int numPistas = 1;
        int numPuertas = 3;

        // 1. INICIAMOS EL LOGGER
        aeron.util.Logger.setup("SEQUENTIAL", numAviones, numPistas, numPuertas, 0);

        // 2. Torre Secuencial
        aeron.sequential.ControlTower tower = new aeron.sequential.ControlTower();

        // 3. Ventana (Opcional en secuencial, pero recomendable)
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        aeron.util.Logger.setWindow(ventana);

        // 4. Ejecución
        for (int i = 1; i <= numAviones; i++) {
            String id = "IB-SEQ-" + String.format("%02d", i);
            aeron.model.Airplane avion = new aeron.model.Airplane(id, tower);
            avion.run();
        }

        // Cerrar logger al acabar en secuencial
        aeron.util.Logger.close();
    }
}