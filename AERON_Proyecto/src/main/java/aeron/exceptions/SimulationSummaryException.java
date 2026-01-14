package aeron.exceptions;

/**
 * Excepción lanzada si falla la generación del informe final (CSV).
 * Garantiza que el usuario sepa si las estadísticas de la simulación no se han guardado.
 */
public class SimulationSummaryException extends AeronException {

    public SimulationSummaryException(String fileName) {
        super("Error generando el resumen de la simulación en el archivo: " + fileName);
    }
}