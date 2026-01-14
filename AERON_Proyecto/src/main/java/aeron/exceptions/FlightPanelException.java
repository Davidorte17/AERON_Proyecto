package aeron.exceptions;

public class FlightPanelException extends AeronException {
    public FlightPanelException() {
        super("No se ha actualizado el panel de vuelos. Fichero JSON no encontrado");
    }
}