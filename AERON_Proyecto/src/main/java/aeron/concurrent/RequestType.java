package aeron.concurrent;

/**
 * Enumerado que define los tipos de operaciones posibles que un avión puede solicitar
 * a la Torre de Control. Facilita la gestión del switch-case en la lógica del Operario.
 */
public enum RequestType {
    LANDING,    // Solicitud de aterrizaje (Necesita Pista + Puerta)
    TAKEOFF,    // Solicitud de despegue (Necesita Pista)
    // Estados para el visualizador (opcionales)
    LANDED,     // Notificación de aterrizaje completado (Libera Pista)
    BOARDING,   // Embarcando
    BOARDED,    // Notificación de embarque completado (Libera Puerta)
    DEPARTED    // Notificación de salida del espacio aéreo (Libera Pista)
}