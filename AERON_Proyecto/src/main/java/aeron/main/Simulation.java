package aeron.main;

import aeron.concurrent.ControlTowerConcurrent;
import aeron.model.Airplane;
import aeron.net.DashboardServer;

public class Simulation {

    // CAMBIAMOS ESTO PARA PROBAR UN MODO U OTRO
    // El enunciado pide que se pueda elegir aquí
    private static final SimulationMode MODE = SimulationMode.CONCURRENT;

    public static DashboardServer server;

    public static void main(String[] args) {
        if (MODE == SimulationMode.SEQUENTIAL) {
            runSequential();
        } else {
            runConcurrent();
        }
    }

    private static void runConcurrent() {
        System.out.println("--- INICIANDO MODO CONCURRENTE (SISTEMA DISTRIBUIDO) ---");

        // Configuramos de parámetros
        int numAviones = 20;
        int numPistas = 3;
        int numPuertas = 5;
        int numOperarios = 5;

        // 1. INICIAR SERVIDOR
        try {
            server = new aeron.net.DashboardServer(9999);
            server.start();
            System.out.println("✅ [SERVIDOR] Listo en puerto 9999.");
        } catch (Exception e) {
            System.err.println("❌ [SERVIDOR] Error: " + e.getMessage());
        }

        // =============================================================
        // ✋ ESPERA AUTOMÁTICA DE CLIENTES
        // =============================================================
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│  ⏳ ESPERANDO AL PANEL REMOTO...                       │");
        System.out.println("│  --> Ejecuta ahora 'RemotePanel' para continuar.       │");
        System.out.println("└────────────────────────────────────────────────────────┘");

        // Bucle que comprueba cada medio segundo si alguien se ha conectado
        while (server.getNumClients() == 0) {
            try {
                Thread.sleep(500);
                System.out.print("."); // Efecto visual de espera
            } catch (InterruptedException e) {}
        }
        System.out.println("\n\n🔌 ¡CLIENTE DETECTADO! LANZANDO SIMULACIÓN... 🚀\n");
        // =============================================================

        // 2. INICIAMOS EL LOGGER
        aeron.util.Logger.setup("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);

        // 3. Creamos la Torre
        // Nota: La creación de Pistas (PISx) y Puertas (GATE x) se hace DENTRO del constructor de la torre
        ControlTowerConcurrent tower = new ControlTowerConcurrent(numPistas, numPuertas);

        // CONTRATAR OPERARIOS (Formato OP-xxx gestionado dentro de la clase Operario)
        for (int i = 1; i <= numOperarios; i++) {
            aeron.concurrent.Operario op = new aeron.concurrent.Operario(tower, i);
            new Thread(op).start();
        }

        // 4. ABRIMOS LA VENTANA
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        aeron.util.Logger.setWindow(ventana);

        // 5. Creamos y lanzamos los aviones con el FORMATO NUEVO (IBE-001)
        for (int i = 1; i <= numAviones; i++) {
            // CAMBIO: Formato del PDF "IBE-" seguido de 3 dígitos (001, 002...)
            String flightId = "IBE-" + String.format("%03d", i);

            // Registramos el avion en el JSON con estado inicial
            aeron.util.AirportJson.actualizarEstado(flightId, "IN_FLIGHT");

            aeron.model.Airplane avion = new aeron.model.Airplane(flightId, tower);
            new Thread(avion).start();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }

        aeron.util.AirportStats.generarResumen(numAviones, numPistas);
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

        // 3. Ventana
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        aeron.util.Logger.setWindow(ventana);

        // 4. Ejecución
        for (int i = 1; i <= numAviones; i++) {
            // Actualizamos también el secuencial para que sea parecido (IBE-SEQ-001)
            String id = "IBE-SEQ-" + String.format("%03d", i);
            aeron.model.Airplane avion = new aeron.model.Airplane(id, tower);
            avion.run();
        }

        // Cerrar logger al acabar en secuencial
        aeron.util.Logger.close();
    }
}