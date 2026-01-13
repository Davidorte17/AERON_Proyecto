package aeron;

public class Simulation {
    public static void main(String[] args) {
        // Configuramos el Logger para modo Secuencial (prueba inicial)
        // 2 Aviones, 1 Pista, 1 Puerta, 0 Operarios
        Logger.setup("SEQUENTIAL", 2, 1, 1, 0);

        Logger.log("Iniciando simulación AERON...");

        // Creamos objetos de prueba
        ControlTower torre = new ControlTower();
        Airplane avion1 = new Airplane("IBE-001");

        Logger.log("Avión creado: " + avion1.getId());

        Logger.log("Fin de la simulación de prueba.");
        Logger.close();
    }
}