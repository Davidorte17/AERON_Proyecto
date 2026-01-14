package aeron.exceptions;

/**
 * Excepción técnica encapsulada para errores de escritura en el sistema de logs.
 * <p>
 * En lugar de propagar una IOException genérica de Java, lanzamos esta excepción propia
 * para indicar que el fallo ocurrió específicamente en el módulo de Logging del aeropuerto.
 */
public class LogException extends AeronException {

    /**
     * Constructor.
     * @param fileName Nombre del fichero que ha fallado.
     */
    public LogException(String fileName) {
        // Mensaje estandarizado de error de acceso a disco.
        super("No se ha encontrado el archivo de log (" + fileName + ") o no se puede escribir en él.");
    }
}