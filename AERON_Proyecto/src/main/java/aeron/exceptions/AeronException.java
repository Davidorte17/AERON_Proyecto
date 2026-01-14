package aeron.exceptions;

/**
 * Clase base para todas las excepciones definidas en el proyecto AERON.
 * <p>
 * Creamos esta jerarquía de excepciones para diferenciar los errores propios de nuestra
 * lógica de negocio (como saturación de pistas o fallos en la torre) de los errores
 * técnicos de Java (como NullPointerException).
 * Al heredar de 'Exception', obligamos a que sean comprobadas (checked),
 * forzando al programador a gestionarlas con try-catch.
 */
public class AeronException extends Exception {

    /**
     * Constructor genérico.
     * @param message Mensaje descriptivo del error que pasaremos a la superclase.
     */
    public AeronException(String message) {
        super(message);
    }
}