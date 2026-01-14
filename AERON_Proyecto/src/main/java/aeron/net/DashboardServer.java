package aeron.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor TCP que implementa la parte distribuidas del sistema (Práctica 7).
 * <p>
 * FUNCIONALIDAD:
 * Escucha conexiones entrantes en un puerto específico y mantiene una lista de
 * clientes conectados (los Paneles Remotos). Actúa como un "repetidor" (Broadcaster):
 * cuando la Torre notifica un cambio, este servidor lo reenvía a todos los clientes.
 * <p>
 * CONCURRENCIA:
 * Extiende de 'Thread' para ejecutarse en paralelo a la simulación principal,
 * evitando que el bloqueo de red (esperar conexiones) detenga el movimiento de los aviones.
 */
public class DashboardServer extends Thread {

    // Puerto TCP donde escucharemos (ej: 9999)
    private int port;

    // El socket del servidor que acepta las conexiones
    private ServerSocket serverSocket;

    // Lista de flujos de salida. Guardamos los 'PrintWriter' de cada cliente conectado
    // para poder enviarles mensajes más tarde.
    // RECURSO COMPARTIDO: Se accede desde el hilo del Server (al añadir) y desde los hilos de la Simulación (al enviar).
    private List<PrintWriter> connectedClients;

    /**
     * Constructor del servidor.
     * @param port El puerto donde se abrirá el servicio.
     */
    public DashboardServer(int port) {
        this.port = port;
        this.connectedClients = new ArrayList<>();
    }

    /**
     * Devuelve el número de clientes conectados actualmente.
     * Utilizado por la clase 'Simulation' para implementar la espera activa al inicio.
     */
    // Para saber si hay alguien conectado
    public int getNumClients() {
        // Sincronizamos el acceso a la lista porque podría estar modificándose
        // justo cuando preguntamos su tamaño.
        synchronized (connectedClients) {
            return connectedClients.size();
        }
    }

    /**
     * Ciclo de vida del hilo del servidor.
     * Se encarga de aceptar nuevas conexiones y registrarlas.
     */
    @Override
    public void run() {
        try {
            // Abrimos el puerto TCP
            serverSocket = new ServerSocket(port);
            System.out.println("[SERVIDOR] Escuchando conexiones en puerto " + port);

            // Bucle infinito para aceptar múltiples clientes (pueden conectarse N paneles)
            while (true) {
                // 1. ACEPTAR CONEXIÓN (Bloqueante)
                // El hilo se detiene aquí hasta que un cliente (RemotePanel) intenta conectarse.
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVIDOR] Nuevo Panel conectado desde: " + clientSocket.getInetAddress());

                // 2. OBTENER FLUJO DE SALIDA
                // Creamos un PrintWriter para enviarle texto a este cliente específico.
                // 'true' activa el auto-flush (envío inmediato sin buffer).
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // 3. REGISTRAR CLIENTE (Sección Crítica)
                // Usamos un bloque synchronized para proteger la lista.
                // Evitamos conflictos si intentamos añadir un cliente mientras
                // se está enviando un mensaje broadcast desde otro hilo.
                synchronized (connectedClients) {
                    connectedClients.add(out);
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error o cierre del servidor: " + e.getMessage());
        }
    }

    /**
     * Envía una actualización de estado a TODOS los clientes conectados.
     * Este método es llamado por el Logger/Torre cada vez que ocurre algo relevante.
     * * @param flightId ID del avión (ej: IBE-001).
     * @param status Nuevo estado (ej: LANDED).
     */
    // Metodo que llamará la Torre/Logger para enviar novedades
    public void broadcastUpdate(String flightId, String status) {
        // Definimos un protocolo de aplicación simple basado en texto: "CLAVE:VALOR"
        String message = flightId + ":" + status; // Protocolo simple: "IBE-001:LANDED"

        // EXCLUSIÓN MUTUA (Lectores-Escritores):
        // Bloqueamos la lista para iterarla. Si no lo hiciéramos y llegara un cliente nuevo
        // justo ahora, saltaría una ConcurrentModificationException.
        synchronized (connectedClients) {
            for (PrintWriter out : connectedClients) {
                // Enviamos el mensaje por la red
                out.println(message);
            }
        }
    }

    /**
     * Cierra el socket del servidor para liberar el puerto.
     */
    public void close() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {}
    }
}