package aeron.util;

import aeron.exceptions.SimulationSummaryException;
import java.io.FileWriter;
import java.io.IOException;

public class AirportStats {

    public static void generarResumen(int numAviones, int numPistas) {
        String fileName = "resumen_simulacion.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Concepto;Valor\n");
            writer.write("Total Aviones;" + numAviones + "\n");
            writer.write("Total Pistas;" + numPistas + "\n");
            writer.write("Estado;Finalizado con Éxito\n");

            System.out.println("Resumen CSV generado correctamente.");

        } catch (IOException e) {
            // REQUISITO P6: Capturar excepción de CSV
            try {
                throw new SimulationSummaryException(fileName);
            } catch (SimulationSummaryException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}