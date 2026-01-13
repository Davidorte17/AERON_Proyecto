package aeron;

public class Simulation {
    public static void main(String[] args) {
        // Configuramos Logger
        Logger.setup("SEQUENTIAL", 1, 1, 1, 0);
        AirportWindow window = new AirportWindow(); // Creamos la ventana (aunque aún no pinta nada sola)

        Logger.log("--- INICIO SIMULACIÓN SECUENCIAL ---");

        ControlTower torre = new ControlTower();

        // Creamos el avión y le pasamos la torre
        Airplane avion1 = new Airplane("IBE-001", torre);

        // Ejecutamos el ciclo del avión (como metodo normal, no como hilo todavía)
        avion1.run();

        Logger.log("--- FIN SIMULACIÓN ---");
        Logger.close();
    }
}