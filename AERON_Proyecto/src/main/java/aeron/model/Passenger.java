package aeron.model;

/**
 * Representa a un pasajero individual de un avión.
 * <p>
 * NOTA DE DISEÑO:
 * Se incluye esta clase para cumplir con el modelo de objetos del dominio "Aeropuerto".
 * Sin embargo, se ha decidido que sea una clase pasiva (sin lógica ni hilos propios).
 * <p>
 * JUSTIFICACIÓN:
 * El foco de la práctica es la sincronización de recursos críticos (Pistas y Puertas)
 * gestionados por la Torre. Los pasajeros son entidades internas del Avión y su
 * comportamiento individual no afecta al algoritmo de asignación de recursos de la Torre.
 * Modelarlos como hilos independientes saturaría el sistema sin aportar valor a la solución de concurrencia.
 */
public class Passenger {
    // La práctica dice que simulemos pasajeros, pero no exige lógica compleja por ahora.
    // Basta con saber a qué avión pertenecen o que existen como concepto.

    // Futura implementación:
    // private String idPasajero;
    // private String idVuelo;

    // Constructor vacío por defecto ya que no requerimos datos específicos para esta simulación.
    public Passenger() {
    }
}