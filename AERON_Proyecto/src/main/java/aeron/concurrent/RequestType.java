package aeron.concurrent;

public enum RequestType {
    LANDING,    // Quiere aterrizar
    TAKEOFF,    // Quiere despegar
    // Estados para el visualizador (opcionales)
    LANDED,     // Ha terminado de aterrizar
    BOARDING,   // Embarcando
    BOARDED,    // Ha terminado de embarcar
    DEPARTED    // Ha despegado
}