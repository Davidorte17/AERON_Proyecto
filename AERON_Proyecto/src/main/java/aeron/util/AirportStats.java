package aeron.util;

import aeron.exceptions.SimulationSummaryException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AirportStats {

    // Lista thread-safe para guardar los registros de cada avión
    // Guardamos cadenas ya formateadas: "IBE-001;4500;1º"
    private static List<FlightRecord> registros = Collections.synchronizedList(new ArrayList<>());

    // Clase interna para facilitar el ordenamiento
    static class FlightRecord implements Comparable<FlightRecord> {
        String id;
        long tiempo;

        public FlightRecord(String id, long tiempo) {
            this.id = id;
            this.tiempo = tiempo;
        }

        @Override
        public int compareTo(FlightRecord o) {
            return Long.compare(this.tiempo, o.tiempo); // Ordenar por tiempo (opcional) o llegada
        }
    }

    /**
     * Método que llama cada avión al terminar su ciclo para registrar sus estadísticas.
     */
    public static void registrarVuelo(String id, long tiempoTotal) {
        registros.add(new FlightRecord(id, tiempoTotal));
    }

    public static void generarResumen() {
        String fileName = "resumen_simulacion.csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            // Cabecera exacta que pide el PDF
            writer.write("Avión;Tiempo total (ms);Observaciones\n");

            // Escribimos cada fila
            int ranking = 1;
            synchronized (registros) {
                for (FlightRecord reg : registros) {
                    // Formato: IBE-001;4520;1º
                    writer.write(String.format("%s;%d;%dº\n", reg.id, reg.tiempo, ranking++));
                }
            }

            System.out.println("📄 [CSV] Resumen generado: " + fileName);

        } catch (IOException e) {
            // REQUISITO P6: Capturar excepción
            try {
                throw new SimulationSummaryException(fileName);
            } catch (SimulationSummaryException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}