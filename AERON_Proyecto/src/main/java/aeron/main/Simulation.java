package aeron.main;

import aeron.concurrent.ControlTowerConcurrent;
import aeron.model.Airplane;
import aeron.net.DashboardServer;

/**
 * Clase Principal (Main) que orquesta toda la ejecución del proyecto AERON.
 * <p>
 * Responsabilidades:
 * 1. Seleccionar el modo de ejecución (Secuencial vs Concurrente).
 * 2. Inicializar la infraestructura (Logger, Ventana, Servidor de Sockets).
 * 3. Instanciar la Torre de Control adecuada.
 * 4. Lanzar los hilos de los actores (Operarios y Aviones).
 */
public class Simulation {

    // CAMBIAMOS ESTO PARA PROBAR UN MODO U OTRO
    // El enunciado pide que se pueda elegir aquí
    // DEFENSA: Cambiando esta variable alternamos entre la Práctica 2 y la Práctica 7.
    private static final SimulationMode MODE = SimulationMode.CONCURRENT;

    // Referencia estática al servidor para que el Logger pueda acceder a él fácilmente
    // y enviar mensajes de broadcast.
    public static DashboardServer server;

    /**
     * Punto de entrada de la aplicación.
     * Despacha la ejecución según el modo configurado.
     */
    public static void main(String[] args) {
        if (MODE == SimulationMode.SEQUENTIAL) {
            runSequential();
        } else {
            runConcurrent();
        }
    }

    /**
     * Configuración del escenario CONCURRENTE (Prácticas 3 a 7).
     * Levanta el sistema completo con Hilos, Semáforos, Monitores y Sockets.
     */
    private static void runConcurrent() {
        System.out.println("--- INICIANDO MODO CONCURRENTE (SISTEMA DISTRIBUIDO) ---");

        // Configuramos los parámetros de la simulación
        int numAviones = 20;
        int numPistas = 3;
        int numPuertas = 5;
        int numOperarios = 5;

        // 1. INICIAR SERVIDOR (Práctica 7)
        try {
            // Arrancamos el servidor en un hilo aparte para aceptar conexiones del Panel Remoto
            server = new aeron.net.DashboardServer(9999);
            server.start();
            System.out.println("✅ [SERVIDOR] Listo en puerto 9999.");
        } catch (Exception e) {
            System.err.println("❌ [SERVIDOR] Error: " + e.getMessage());
        }

        // --- AUTO-LANZAMIENTO ---
        lanzarRemotePanelAutomaticamente();
        // -------------------------------

        // Bucle que comprueba cada medio segundo si alguien se ha conectado
        while (server.getNumClients() == 0) {
            try {
                Thread.sleep(500);
                System.out.print("."); // Efecto visual de espera
            } catch (InterruptedException e) {}
        }
        System.out.println("\n\n🔌 ¡CLIENTE DETECTADO! LANZANDO SIMULACIÓN... 🚀\n");
        // =============================================================

        // 2. INICIAMOS EL LOGGER (Práctica 1 y 6)
        // Preparamos los ficheros y carpetas de logs
        aeron.util.Logger.setup("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);

        // 3. CREAMOS LA TORRE (Práctica 4 - Monitor y Semáforos)
        // Nota: La creación de Pistas (PISx) y Puertas (GATE x) se hace DENTRO del constructor de la torre
        ControlTowerConcurrent tower = new ControlTowerConcurrent(numPistas, numPuertas);

        // CONTRATAR OPERARIOS (Hilos Consumidores)
        // Creamos los hilos que procesarán la cola de peticiones
        for (int i = 1; i <= numOperarios; i++) {
            aeron.concurrent.Operario op = new aeron.concurrent.Operario(tower, i);
            new Thread(op).start(); // .start() inicia un nuevo hilo de ejecución
        }

        // 4. ABRIMOS LA VENTANA (GUI Swing)
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        // Vinculamos la ventana al logger para que reciba los mensajes
        aeron.util.Logger.setWindow(ventana);

        // 5. LANZAMOS LOS AVIONES (Hilos Productores)
        for (int i = 1; i <= numAviones; i++) {
            // CAMBIO: Formato del PDF "IBE-" seguido de 3 dígitos (001, 002...)
            String flightId = "IBE-" + String.format("%03d", i);

            // PRÁCTICA 5: Registramos el avion en el JSON con estado inicial
            aeron.util.AirportJson.actualizarEstado(flightId, "IN_FLIGHT");

            // Creamos la instancia y el hilo
            aeron.model.Airplane avion = new aeron.model.Airplane(flightId, tower);
            new Thread(avion).start(); // .start() es vital para que sea concurrente

            // Pequeña pausa para escalonar las llegadas y no saturar el log instantáneamente
            try { Thread.sleep(2500); } catch (InterruptedException e) {}
        }

        // Al terminar el lanzamiento, generamos el resumen estadístico
        aeron.util.AirportStats.generarResumen();
    }

    /**
     * Configuración del escenario SECUENCIAL (Práctica 2).
     * Ejecuta todo en un único hilo (el main), sin concurrencia real.
     */
    private static void runSequential() {
        System.out.println("--- INICIANDO MODO SECUENCIAL ---");

        int numAviones = 20;
        int numPistas = 3;
        int numPuertas = 5;

        // 1. INICIAMOS EL LOGGER
        aeron.util.Logger.setup("SEQUENTIAL", numAviones, numPistas, numPuertas, 0);

        // 2. Torre Secuencial (Implementación simple sin semáforos)
        aeron.sequential.ControlTower tower = new aeron.sequential.ControlTower();

        // 3. Ventana
        aeron.util.AirportWindow ventana = new aeron.util.AirportWindow();
        ventana.setVisible(true);
        aeron.util.Logger.setWindow(ventana);

        // 4. Ejecución Lineal
        for (int i = 1; i <= numAviones; i++) {
            // Actualizamos también el secuencial para que sea parecido (IBE-SEQ-001)
            String id = "IBE-SEQ-" + String.format("%03d", i);
            aeron.model.Airplane avion = new aeron.model.Airplane(id, tower);

            // DEFENSA: Fíjate que aquí llamamos a .run() DIRECTAMENTE.
            // Esto NO crea un hilo nuevo. El código del avión se ejecuta en el hilo 'main'
            // bloqueando el bucle hasta que el avión termina todo su ciclo.
            // Por eso es "Secuencial".
            avion.run();
        }

        // Cerrar logger al acabar en secuencial
        aeron.util.Logger.close();
    }

    // Método mágico para lanzar el otro programa automáticamente
    private static void lanzarRemotePanelAutomaticamente() {
        try {
            System.out.println("🤖 Lanzando Panel Remoto automáticamente...");

            // 1. Obtenemos la ruta del ejecutable de Java actual
            String javaBin = System.getProperty("java.home") + "/bin/java";

            // 2. Obtenemos el ClassPath actual (dónde están tus clases compiladas y librerías)
            String classpath = System.getProperty("java.class.path");

            // 3. Definimos la clase que queremos ejecutar
            String className = aeron.net.RemotePanel.class.getName();

            // 4. Construimos el comando
            ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);

            // IMPORTANTE: Redirigimos la salida para ver errores si los hay,
            // aunque al hacerlo gráfico (siguiente paso) ya no dependeremos de la consola.
            builder.inheritIO();

            // 5. ¡Fuego! 🔥
            builder.start();

        } catch (Exception e) {
            System.err.println("Error al intentar lanzar el panel remoto: " + e.getMessage());
        }
    }
}