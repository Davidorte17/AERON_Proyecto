package aeron.exceptions;

/**
 * Excepción específica para fallos en la actualización del Panel de Vuelos (JSON).
 * <p>
 * Se utiliza en la clase AirportJson cuando no es posible volcar el estado
 * de los aviones al fichero 'aeropuerto.json'.
 */
public class FlightPanelException extends AeronException {

    public FlightPanelException() {
        // Mensaje de error requerido por el enunciado de la Práctica 6.
        super("No se ha actualizado el panel de vuelos. Fichero JSON no encontrado o inaccesible.");
    }
}