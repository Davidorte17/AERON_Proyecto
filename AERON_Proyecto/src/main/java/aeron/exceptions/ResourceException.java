package aeron.exceptions;

public class ResourceException extends AeronException {
    public ResourceException(String recurso, String idAvion) {
        super("No se ha podido asignar " + recurso + " al avión " + idAvion);
    }
}