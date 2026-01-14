package aeron.exceptions;

public class SimulationSummaryException extends AeronException {
    public SimulationSummaryException(String fileName) {
        super("Error al escribir el resumen de la simulación. No se ha podido guardar en el fichero " + fileName);
    }
}