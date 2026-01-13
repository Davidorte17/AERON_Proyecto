package aeron;

// Estados definidos en la Práctica 2
public enum FlightStatus {
    IN_FLIGHT,          // En vuelo
    LANDING_REQUEST,    // Aterrizaje solicitado
    LANDING_ASSIGNED,   // Aterrizaje autorizado (tiene pista y puerta)
    LANDING,            // Aterrizando (durante un tiempo)
    LANDED,             // Aterrizado (libera pista)
    BOARDING,           // Embarcando pasajeros
    BOARDED,            // Embarcado (libera puerta)
    TAKEOFF_REQUESTED,  // Despegue solicitado
    TAKEOFF_ASSIGNED,   // Despegue autorizado
    DEPARTING,          // Despegando
    DEPARTED            // Despegado (fin del ciclo)
}