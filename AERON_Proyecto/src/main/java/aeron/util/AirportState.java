package aeron.util;

import java.util.ArrayList;
import java.util.List;

// importa clase de Request (peticiones)
import aeron.concurrent.Request;
import aeron.concurrent.ControlTowerConcurrent;

// importa clase de Runway (pista)
import aeron.util.Runway;

// importa clase de Gate (puerta)
import aeron.util.Gate;

/**
 * Clase de utilidad estática encargada de la "Visualización" del sistema en los logs.
 * <p>
 * Su única responsabilidad es tomar el estado actual de los objetos (colas, pistas, puertas)
 * y convertirlos en Strings formateados (ASCII Art) con bordes y emojis.
 * Esto facilita enormemente la depuración visual de la concurrencia.
 */
public class AirportState {

    /**
     * Muestra el contenido de la cola de peticiones (FIFO) con una separación
     * visible arriba y abajo para que se vea claramente la cola.
     * <p>
     * Recorre la lista de peticiones y traduce los ENUMs técnicos a iconos visuales
     * para que sea más fácil entender qué está esperando cada avión.
     * * @param requestQueue La lista de peticiones a dibujar (copia segura pasada por la Torre).
     * @return String formateado con el cuadro.
     */
    public static String showRequestQueue(List<Request> requestQueue) {
        int n = requestQueue.size();
        List<String> lines = new ArrayList<>();
        String title = "Cola de peticiones (" + n + ")";

        // Caso base: Si la cola está vacía, mostramos un mensaje simple
        if (n == 0) {
            String empty = title + ": vacía";
            // sin márgenes laterales, el ancho es justo el de la línea
            int innerWidth = empty.length();
            String border = repeat('═', innerWidth);
            StringBuilder sbEmpty = new StringBuilder();
            sbEmpty.append("╔").append(border).append("╗\n");
            sbEmpty.append(empty).append("\n");
            sbEmpty.append("╚").append(border).append("╝");
            return sbEmpty.toString();
        }

        lines.add(title + ":");
        int i = 1;
        // Iteramos sobre las peticiones para formatear cada línea
        for (Request r : requestQueue) {
            String tipo;
            // Usamos un switch para asignar emojis según el tipo de petición
            switch (r.type) {
                case LANDING:
                    tipo = "Aterrizaje 🛬";
                    break;
                case LANDED:
                    tipo = "Aterrizado ✅";
                    break;
                case BOARDED:
                    tipo = "Embarcado 🧳";
                    break;
                case TAKEOFF:
                    tipo = "Despegue 🛫";
                    break;
                case DEPARTED:
                    tipo = "Despegado ✈️";
                    break;
                default:
                    tipo = r.type.toString();
                    break;
            }
            lines.add("  " + i++ + ") " + tipo + " — " + r.plane);
        }

        // calcular ancho máximo de las líneas para la barra superior/inferior
        // Esto es necesario para que el cuadro ASCII quede cuadrado y bonito
        int max = 0;
        for (String l : lines) {
            if (l.length() > max)
                max = l.length();
        }

        // Construimos el cuadro final
        String border = repeat('═', max);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔").append(border).append("╗\n");
        for (String l : lines) {
            sb.append(l);
            // rellenar espacios a la derecha para que coincida con el ancho del borde
            int padding = max - l.length();
            if (padding > 0) {
                sb.append(repeat(' ', padding));
            }
            sb.append("\n");
        }
        sb.append("╚").append(border).append("╝");

        return sb.toString();
    }

    /**
     * Método auxiliar privado para repetir un carácter N veces.
     * Útil para generar los bordes horizontales de las tablas (══════).
     */
    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Genera una representación visual del estado de los recursos físicos (Pistas y Puertas).
     * Muestra IDs y un indicador de color (Rojo/Verde) según su ocupación.
     * * @param runways Lista de pistas del aeropuerto.
     * @param gates Lista de puertas del aeropuerto.
     * @return String formateado con la tabla de estado.
     */
    // metodo para ver el estado de las pistas y de las puertas
    public static String showResourcesStatus(List<Runway> runways, List<Gate> gates) {
        List<String> lines = new ArrayList<>();
        lines.add("Estado de recursos:");

        // --- SECCIÓN PISTAS ---
        // Pistas: ids en una línea, iconos justo debajo alineados por columna
        if (runways.isEmpty()) {
            lines.add("Pistas: (vacías)");
        } else {
            lines.add("Pistas:");
            int nR = runways.size();
            String[] idsR = new String[nR];
            String[] iconsR = new String[nR];
            int[] colW = new int[nR];

            // Preparamos los datos
            for (int i = 0; i < nR; i++) {
                Runway r = runways.get(i);
                idsR[i] = r.getId();
                // 🟢 = Disponible (True), 🔴 = Ocupada (False)
                iconsR[i] = r.isAvailable() ? "🟢" : "🔴";
                colW[i] = Math.max(idsR[i].length(), iconsR[i].length());
            }

            // Construimos las dos líneas (Cabeceras y Estado)
            StringBuilder idLine = new StringBuilder();
            StringBuilder iconLine = new StringBuilder();
            for (int i = 0; i < nR; i++) {
                idLine.append(idsR[i]);
                iconLine.append(iconsR[i]);
                // Ajuste de espaciado (padding) para alinear columnas
                int padId = colW[i] - idsR[i].length();
                int padIcon = colW[i] - iconsR[i].length();
                if (padId > 0)
                    idLine.append(repeat(' ', padId));
                if (padIcon > 0)
                    iconLine.append(repeat(' ', padIcon));
                if (i < nR - 1) {
                    idLine.append("   "); // separación entre columnas
                    iconLine.append("   ");
                }
            }
            lines.add(idLine.toString());
            lines.add(iconLine.toString());
        }

        // Separador visual entre secciones
        lines.add("");

        // --- SECCIÓN PUERTAS ---
        // Puertas: mismo formato que pistas, pero comprobando isOccupied()
        if (gates.isEmpty()) {
            lines.add("Puertas: (vacías)");
        } else {
            lines.add("Puertas:");
            int nG = gates.size();
            String[] idsG = new String[nG];
            String[] iconsG = new String[nG];
            int[] colWg = new int[nG];

            for (int i = 0; i < nG; i++) {
                Gate g = gates.get(i);
                idsG[i] = g.getId();
                // Aquí la lógica es inversa: isOccupied() true -> Rojo
                iconsG[i] = g.isOccupied() ? "🔴" : "🟢";
                colWg[i] = Math.max(idsG[i].length(), iconsG[i].length());
            }

            StringBuilder idLineG = new StringBuilder();
            StringBuilder iconLineG = new StringBuilder();
            for (int i = 0; i < nG; i++) {
                idLineG.append(idsG[i]);
                iconLineG.append(iconsG[i]);
                int padId = colWg[i] - idsG[i].length();
                int padIcon = colWg[i] - iconsG[i].length();
                if (padId > 0)
                    idLineG.append(repeat(' ', padId));
                if (padIcon > 0)
                    iconLineG.append(repeat(' ', padIcon));
                if (i < nG - 1) {
                    idLineG.append("   ");
                    iconLineG.append("   ");
                }
            }
            lines.add(idLineG.toString());
            lines.add(iconLineG.toString());
        }

        // calcular ancho máximo de las líneas para la barra superior/inferior
        int max = 0;
        for (String l : lines) {
            if (l.length() > max)
                max = l.length();
        }

        // Enmarcar todo en un cuadro
        String border = repeat('═', max);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔").append(border).append("╗\n");
        for (String l : lines) {
            sb.append(l);
            int padding = max - l.length();
            if (padding > 0) {
                sb.append(repeat(' ', padding));
            }
            sb.append("\n");
        }
        sb.append("╚").append(border).append("╝");

        return sb.toString();
    }

}