package aeron;

import javax.swing.*;
import java.awt.*;

public class AirportWindow extends JFrame {
    // Componente de texto donde escribiremos los logs
    private JTextArea logArea;

    public AirportWindow() {
        // Configuración básica de la ventana
        setTitle("Simulación Aeropuerto AERON");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de texto (no editable) para mostrar eventos
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Añadimos scroll por si hay mucho texto
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Hacemos visible la ventana
        setVisible(true);
    }

    // Metodo para añadir texto a la ventana desde fuera
    public void addLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            // Auto-scroll hacia abajo
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}