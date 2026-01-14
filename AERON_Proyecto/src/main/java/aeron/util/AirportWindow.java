package aeron.util;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana gráfica (GUI) basada en Swing para visualizar la simulación.
 * <p>
 * Su objetivo es mostrar en tiempo real los mismos mensajes que aparecen en la consola,
 * facilitando el seguimiento visual de los eventos sin perder el historial gracias al scroll.
 */
public class AirportWindow extends JFrame {

    // Componente de texto multilínea donde iremos acumulando los logs
    private JTextArea logArea;

    /**
     * Constructor de la ventana.
     * Configuramos el tamaño, título y disposición de los elementos.
     */
    public AirportWindow() {
        // Configuramos las propiedades básicas de la ventana principal
        setTitle("Simulación Aeropuerto AERON");
        setSize(800, 600);

        // Aseguramos que al cerrar la ventana, se detenga la aplicación
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Usamos un BorderLayout para que el texto ocupe todo el espacio central
        setLayout(new BorderLayout());

        // Inicializamos el área de texto
        logArea = new JTextArea();
        logArea.setEditable(false); // Bloqueamos la edición para que sea solo de lectura

        // Usamos fuente monoespaciada para que las tablas ASCII (Practica 7) se alineen bien
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Envolvemos el área de texto en un ScrollPane para tener barras de desplazamiento
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Hacemos visible la ventana en la pantalla
        setVisible(true);
    }

    /**
     * Metodo thread-safe para añadir texto a la ventana desde cualquier hilo.
     * @param text El mensaje de log a mostrar.
     */
    public void addLog(String text) {
        // IMPORTANTE: Swing no es Thread-Safe (no es seguro para hilos).
        // Como este metodo lo llaman los hilos de los Aviones y Operarios,
        // no podemos tocar el JTextArea directamente o la interfaz podría corromperse.
        // Usamos 'invokeLater' para encolar la actualización en el Hilo de Eventos de Swing (EDT).
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");

            // Hacemos auto-scroll hacia abajo para ver siempre el mensaje más reciente
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}