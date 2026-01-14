package aeron.util;

import aeron.exceptions.SimulationSummaryException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase de utilidad encargada de generar las estadísticas finales de la simulación.
 * <p>
 * FUNCIONALIDAD (Práctica 6):
 * Al terminar la ejecución, vuelca los datos resumen (número de aviones, pistas, etc.)
 * a un archivo de texto con formato CSV (Comma Separated Values) para su posterior análisis.
 */
public class AirportStats {

    /**
     * Genera el archivo 'resumen_simulacion.csv'.
     * Este metodo se invoca al finalizar el bucle principal en Simulation.java.
     * * @param numAviones Cantidad total de aviones procesados en la simulación.
     * @param numPistas Número de pistas que se configuraron.
     */
    public static void generarResumen(int numAviones, int numPistas) {
        String fileName = "resumen_simulacion.csv";

        // Usamos la estructura 'try-with-resources' (Java 7+).
        // Esto garantiza que el FileWriter se cierre automáticamente al terminar el bloque,
        // liberando el recurso del sistema operativo sin necesidad de un bloque 'finally'.
        try (FileWriter writer = new FileWriter(fileName)) {

            // Escribimos la cabecera del CSV con separador de punto y coma (;)
            writer.write("Concepto;Valor\n");

            // Escribimos los datos dinámicos de la simulación
            writer.write("Total Aviones;" + numAviones + "\n");
            writer.write("Total Pistas;" + numPistas + "\n");
            writer.write("Estado;Finalizado con Éxito\n");

            System.out.println("Resumen CSV generado correctamente.");

        } catch (IOException e) {
            // Si falla la escritura (ej: disco lleno, fichero abierto por otro programa),
            // capturamos la excepción genérica y lanzamos la nuestra propia.
            try {
                // Envolvemos el error en nuestra excepción de negocio
                throw new SimulationSummaryException(fileName);
            } catch (SimulationSummaryException ex) {
                // Imprimimos el mensaje de error amigable definido en la excepción
                System.err.println(ex.getMessage());
            }
        }
    }
}