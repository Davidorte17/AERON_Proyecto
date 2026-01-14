package aeron.exceptions;

/**
 * Excepción lanzada cuando la Torre de Control no admite más peticiones.
 * <p>
 * Implementamos esta clase para gestionar el requisito de límite de capacidad en la cola.
 * Si la cola de peticiones supera el tamaño máximo (MAX_COLA), lanzamos este error
 * para notificar que el sistema está saturado.
 */
public class SaturationException extends AeronException {

    /**
     * Constructor específico para el error de saturación.
     * Construimos un mensaje detallado indicando qué operación y qué avión han causado el fallo.
     * * @param operacion Tipo de operación (LANDING, TAKEOFF...)
     * @param idAvion Identificador del avión.
     */
    public SaturationException(String operacion, String idAvion) {
        // Formateamos el mensaje tal como pide el enunciado para el log de errores.
        super(String.format("[TORRE] Cola de peticiones completa, reintentando más tarde la captura de la petición %s del avión %s",
                operacion, idAvion));
    }
}