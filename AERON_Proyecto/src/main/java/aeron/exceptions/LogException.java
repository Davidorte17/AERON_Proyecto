package aeron.exceptions;

public class LogException extends AeronException {
    public LogException(String fileName) {
        super("No se ha encontrado el archivo de log " + fileName);
    }
}