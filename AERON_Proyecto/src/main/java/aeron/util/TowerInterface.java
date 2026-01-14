package aeron.util;

import aeron.model.Airplane;

/**
 * Interfaz que define el contrato de comunicación entre los Aviones y la Torre de Control.
 * <p>
 * DISEÑO:
 * Utilizamos esta interfaz para desacoplar la clase 'Airplane' de la implementación concreta de la torre.
 * Esto permite que el mismo objeto 'Airplane' funcione tanto en la simulación SECUENCIAL (ControlTower)
 * como en la CONCURRENTE (ControlTowerConcurrent) sin necesidad de modificar su código.
 * Simplemente le inyectamos la implementación correspondiente al inicio.
 */
public interface TowerInterface {

    /**
     * metodo principal de entrada a la Torre.
     * El avión invoca este metodo para solicitar operaciones (Aterrizaje, Despegue)
     * o para notificar cambios de estado (Embarcado, Despegado).
     * * @param avion El objeto avión que realiza la petición.
     */
    // En la versión Concurrente, este metodo es el que gestiona el acceso a la cola protegida por semáforos.
    void registrarPeticion(Airplane avion);

    /**
     * metodo explícito para liberar una pista.
     * * @param avion El avión que ha terminado de usar la pista.
     */
    // Mantenemos este metodo para cumplir con el diseño original de la Práctica 2.
    // En la implementación Concurrente avanzada, la liberación se gestiona automáticamente
    // dentro de 'registrarPeticion' al detectar el estado LANDED o DEPARTED.
    void liberarPista(Airplane avion);
}