package aeron.exceptions;

/**
 * Excepción lanzada cuando un avión solicita aterrizar o despegar pero no hay recursos disponibles.
 * <p>
 * Nos permite gestionar el flujo alternativo cuando faltan Pistas o Puertas,
 * evitando que el avión se quede bloqueado indefinidamente sin feedback.
 */
public class ResourceException extends AeronException {

    /**
     * Constructor para informar de la falta de un recurso específico.
     * * @param recurso Tipo de recurso que falta ("Pista" o "Puerta").
     * @param idAvion Identificador del avión afectado.
     */
    public ResourceException(String recurso, String idAvion) {
        // Mensaje informativo para el log de la Torre.
        super(String.format("[TORRE] No se ha podido asignar %s al avión %s", recurso, idAvion));
    }
}