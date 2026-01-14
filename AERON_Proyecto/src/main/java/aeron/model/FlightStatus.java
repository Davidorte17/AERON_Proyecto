package aeron.model;

/**
 * Enumerado que define la MÁQUINA DE ESTADOS del ciclo de vida de un avión.
 * <p>
 * Permite sincronizar la lógica del hilo 'Airplane' con la gestión de la 'ControlTower'.
 * El avión va transicionando secuencialmente por estos estados desde que aparece
 * hasta que abandona el sistema.
 */
// Estados definidos en la Práctica 2
public enum FlightStatus {

    /** Estado inicial cuando se crea el hilo del avión. */
    IN_FLIGHT,          // En vuelo

    /** * El avión ha llegado al aeropuerto y se mete en la cola de peticiones.
     * (Productor produce petición de aterrizaje).
     */
    LANDING_REQUEST,    // Aterrizaje solicitado

    /**
     * ESTADO CRÍTICO DE SINCRONIZACIÓN:
     * La Torre ha asignado recursos (Pista + Puerta) y cambia el avión a este estado.
     * Esto rompe el bucle 'while' de espera en el hilo del avión.
     */
    LANDING_ASSIGNED,   // Aterrizaje autorizado (tiene pista y puerta)

    /** El avión está ocupando la pista físicamente durante un tiempo (Thread.sleep). */
    LANDING,            // Aterrizando (durante un tiempo)

    /** * El avión ha tocado tierra. Sirve para notificar a la Torre que libere la Pista.
     */
    LANDED,             // Aterrizado (libera pista, mantiene puerta)

    /** Simulación del tiempo de carga/descarga de pasajeros. */
    BOARDING,           // Embarcando pasajeros

    /** * El embarque ha finalizado. Sirve para notificar a la Torre que libere la Puerta.
     */
    BOARDED,            // Embarcado (libera puerta)

    /** * El avión solicita permiso para irse. Entra de nuevo en la cola de la Torre.
     */
    TAKEOFF_REQUESTED,  // Despegue solicitado

    /**
     * ESTADO CRÍTICO DE SINCRONIZACIÓN:
     * La Torre ha asignado una Pista libre para salir.
     * Desbloquea el segundo bucle de espera del avión.
     */
    TAKEOFF_ASSIGNED,   // Despegue autorizado

    /** El avión está ocupando la pista de salida (Thread.sleep). */
    DEPARTING,          // Despegando

    /** * Estado final. El avión libera la pista de despegue y el hilo termina su ejecución.
     */
    DEPARTED            // Despegado (fin del ciclo)
}