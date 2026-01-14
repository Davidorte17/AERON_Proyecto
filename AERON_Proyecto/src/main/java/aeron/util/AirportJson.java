package aeron.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AirportJson {
    // Memoria caché de los estados
    private static Map<String, String> estadosAviones = new HashMap<>();
    private static final String FILE_PATH = "aeropuerto.json";

    // --- SOLUCIÓN CON MONITORES (TEMA 5) ---
    // Al poner 'synchronized', Java garantiza Exclusión Mutua.
    // Solo un hilo (avión/torre) puede ejecutar este metodo a la vez.
    public static synchronized void actualizarEstado(String flightId, String nuevoEstado) {
        // 1. Actualizamos memoria
        estadosAviones.put(flightId, nuevoEstado);
        // 2. Escribimos en fichero
        escribirJson();
    }

    // Este metodo es privado y solo se llama desde dentro del synchronized,
    // así que es seguro.
    private static void escribirJson() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write("{\n");

            // Convertimos el mapa a JSON bonito
            String jsonBody = estadosAviones.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> "  \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
                    .collect(Collectors.joining(",\n"));

            writer.write(jsonBody);
            writer.write("\n}");

        } catch (IOException e) {
            try {
                // Lanzamos la excepción específica del Panel
                throw new aeron.exceptions.FlightPanelException();
            } catch (aeron.exceptions.FlightPanelException ex) {
                // Imprimimos el mensaje oficial: "No se ha actualizado el panel de vuelos..."
                System.err.println(ex.getMessage());
            }
        }
    }
}