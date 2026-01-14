package aeron.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente TCP (Práctica 7) que actúa como Panel de Vuelos Remoto.
 * <p>
 * ARQUITECTURA:
 * Este programa es un proceso totalmente independiente de la simulación principal.
 * Se conecta vía Sockets (TCP/IP) al servidor de la Torre para recibir actualizaciones
 * en tiempo real y mostrar una tabla de vuelos replicada.
 */
public class RemotePanel {

    // "Base de datos" local del cliente.
    // Necesitamos este Mapa para guardar el estado de todos los vuelos recibidos,
    // ya que la pantalla se borra y se repinta completa con cada actualización.
    private static Map<String, String> vuelos = new HashMap<>();

    public static void main(String[] args) {
        // Configuración de conexión (Hardcoded a localhost para la práctica)
        String host = "localhost";
        int port = 9999;

        System.out.println("--- PANEL DE VUELOS REMOTO ---");
        System.out.println("Conectando a " + host + ":" + port + "...");

        // Usamos try-with-resources para asegurar que el socket y el reader se cierren bien
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("¡Conectado! Esperando actualizaciones de la Torre...");

            String inputLine;

            // BUCLE DE ESCUCHA INFINITA:
            // El método in.readLine() es BLOQUEANTE. El programa se detiene en esta línea
            // hasta que el Servidor (DashboardServer) envía un mensaje con println().
            // Cuando el servidor cierra la conexión, readLine() devuelve null y salimos.
            while ((inputLine = in.readLine()) != null) {

                // PROTOCOLO DE APLICACIÓN:
                // Hemos definido un formato simple de texto: "ID_VUELO:ESTADO"
                // Ejemplo: "IBE-001:LANDED"
                String[] partes = inputLine.split(":");

                if (partes.length == 2) {
                    String id = partes[0];
                    String estado = partes[1];

                    // 1. Actualizamos nuestra memoria local con el dato fresco
                    vuelos.put(id, estado);

                    // 2. Refrescamos la interfaz de usuario (Consola)
                    limpiarConsola();
                    imprimirTabla();
                }
            }

        } catch (IOException e) {
            // Gestión de errores de red (ej: servidor apagado o conexión rechazada)
            System.err.println("Error de conexión (¿Está la simulación encendida?): " + e.getMessage());
        }
    }

    /**
     * Genera y muestra la tabla ASCII con el estado de todos los vuelos conocidos.
     */
    private static void imprimirTabla() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║       PANEL DE VUELOS REMOTO   ║");
        System.out.println("╠════════════════╤═══════════════╣");
        System.out.println("║ VUELO          │ ESTADO        ║");
        System.out.println("╠════════════════╪═══════════════╣");

        // Ordenamos por ID de vuelo alfabéticamente para que la tabla no baile
        // cada vez que se repinta.
        vuelos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    // Formato de columnas fijo para mantener la alineación
                    System.out.printf("║ %-14s │ %-13s ║%n", entry.getKey(), entry.getValue());
                });

        System.out.println("╚════════════════╧═══════════════╝");
    }

    // Truco simple para "limpiar" consola imprimiendo saltos de línea
    // (Funciona en cualquier sistema operativo sin comandos complejos)
    private static void limpiarConsola() {
        for(int i = 0; i < 5; i++) System.out.println();
    }
}