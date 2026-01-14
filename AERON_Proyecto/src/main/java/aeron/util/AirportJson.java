package aeron.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase de utilidad que gestiona la persistencia del estado del aeropuerto.
 * <p>
 * FUNCIONALIDAD (Práctica 5):
 * Actúa como el "Backend" del Panel de Vuelos. Mantiene un registro actualizado
 * de en qué estado se encuentra cada avión y lo vuelca a un fichero JSON en disco.
 * <p>
 * CONCURRENCIA (Tema 5 - Monitores):
 * Dado que el fichero 'aeropuerto.json' es un RECURSO COMPARTIDO crítico (escrito por
 * múltiples hilos de la Torre/Aviones), protegemos su acceso mediante un MONITOR
 * utilizando la palabra clave 'synchronized'.
 */
public class AirportJson {

    // "Memoria caché" de los estados. Usamos un Mapa para acceso rápido por ID de avión.
    // Evita tener que leer el fichero antes de escribir; solo sobreescribimos.
    private static Map<String, String> estadosAviones = new HashMap<>();

    // Ruta del fichero de salida que simula la base de datos del panel
    private static final String FILE_PATH = "aeropuerto.json";

    /**
     * Metodo público para registrar un cambio de estado.
     * <p>
     * IMPLEMENTACIÓN DEL MONITOR:
     * Al poner 'synchronized', Java garantiza EXCLUSIÓN MUTUA.
     * Solo un hilo (sea un Avión o un Operario de la Torre) puede ejecutar este metodo a la vez.
     * Si otro hilo intenta entrar mientras se está escribiendo el fichero, Java lo pone en espera.
     * * @param flightId Identificador del avión (ej: IBE-001).
     * @param nuevoEstado El nuevo estado a registrar (ej: LANDED).
     */
    public static synchronized void actualizarEstado(String flightId, String nuevoEstado) {
        // 1. Actualizamos la estructura de datos en memoria (rápido)
        estadosAviones.put(flightId, nuevoEstado);

        // 2. Persistimos los cambios en el disco (lento y crítico)
        escribirJson();
    }

    /**
     * Metodo auxiliar privado que realiza la escritura física en el disco.
     * <p>
     * SEGURIDAD:
     * Este metodo NO necesita ser synchronized explícitamente porque es privado
     * y SOLO se llama desde 'actualizarEstado', que ya posee el bloqueo del monitor.
     */
    private static void escribirJson() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write("{\n");

            // Convertimos el mapa a una cadena con formato JSON "bonito" (pretty print).
            // Usamos Streams de Java 8 para ordenar por ID y formatear "Clave": "Valor".
            String jsonBody = estadosAviones.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> "  \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
                    .collect(Collectors.joining(",\n"));

            writer.write(jsonBody);
            writer.write("\n}");

        } catch (IOException e) {
            try {
                // Si falla la escritura en disco (ej: permisos, disco lleno),
                // lanzamos nuestra excepción personalizada definida en el paquete 'exceptions'.
                throw new aeron.exceptions.FlightPanelException();
            } catch (aeron.exceptions.FlightPanelException ex) {
                // Imprimimos el mensaje oficial requerido por el enunciado:
                // "No se ha actualizado el panel de vuelos. Fichero JSON no encontrado"
                System.err.println(ex.getMessage());
            }
        }
    }
}