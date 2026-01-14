package aeron.exceptions;

public class SaturationException extends AeronException {
    public SaturationException(String tipoPeticion, String idAvion) {
        super("Cola de peticiones completa, reintentando más tarde la captura de la petición " + tipoPeticion + " del avión " + idAvion);
    }
}