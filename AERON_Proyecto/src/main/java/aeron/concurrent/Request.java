package aeron.concurrent;

import aeron.concurrent.RequestType;
import aeron.model.Airplane;

/**
 * Clase auxiliar que encapsula una solicitud de un avión para ser procesada por la Torre.
 * <p>
 * Representa el "objeto de datos" que viaja a través del buffer (cola) en el
 * patrón Productor-Consumidor. Permite que la cola almacene de forma uniforme
 * distintos tipos de operaciones (Aterrizajes, Despegues, etc.).
 */
public class Request {

    // El avión que genera la solicitud (el Productor)
    public Airplane plane;

    // El tipo de operación que solicita (LANDING, TAKEOFF, etc.)
    public RequestType type;

    /**
     * Constructor para crear una nueva petición empaquetada.
     * @param plane El avión implicado.
     * @param type La acción que desea realizar.
     */
    public Request(Airplane plane, RequestType type) {
        this.plane = plane;
        this.type = type;
    }

    /**
     * Devuelve una representación en cadena, útil para depuración o logs sencillos.
     * En este caso, devolvemos el ID del avión asociado.
     */
    @Override
    public String toString() { return plane.getId(); }
}