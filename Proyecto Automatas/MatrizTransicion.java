import java.util.Arrays;
import java.util.List;

/**
 * CLASE 3: MATRIZ DE TRANSICION
 * ---------------------------------------------------------------------
 * RESPONSABLE SUGERIDO: Integrante 3
 *
 * Qué debe hacer esta clase:
 *   - Tomar los Estados y transiciones definidos en Automata.java y
 *     "aplanarlos" en una tabla (matriz) de:
 *         filas    = estados
 *         columnas = clases de caracter (letra, digito, etc.)
 *         valor    = estado destino, o -100 (Tokens.ERROR) si no hay
 *                    transición definida para esa combinación.
 *   - Esta clase también debe clasificar cada caracter leído del
 *     archivo fuente en una de las columnas (método claseDe(char)).
 *     Esa función es la que usará Aplicacion.java para "moverse" por
 *     la matriz mientras escanea el .txt de entrada.
 *
 * Qué falta por hacer (trabajo del equipo):
 *   - Si el equipo agrega estados nuevos en Automata.java (para
 *     strings, reales, comentarios), probablemente necesiten agregar
 *     columnas nuevas aquí en COLUMNAS y en claseDe(char). Ejemplo:
 *     ya se dejaron listas "punto", "comilla", "barra", "amper",
 *     "pipe" preparadas para cuando completen esos autómatas.
 *   - Revisar que cada clase de caracter sea mutuamente excluyente
 *     (que un mismo caracter no caiga en dos columnas a la vez).
 */
public class MatrizTransicion {

    // Columnas de la matriz = clases de caracter reconocidas
    public static final List<String> COLUMNAS = Arrays.asList(
            "letra", "digito", "menor", "mayor", "igual",
            "dospuntos", "punto", "comilla", "barra", "amper", "pipe", "otros"
    );

    private int[][] matriz; // matriz[estado][columna] = estado destino o Tokens.ERROR
    private final Automata automata;

    public MatrizTransicion(Automata automata) {
        this.automata = automata;
    }

    /** Construye la matriz numérica a partir de los estados del autómata. */
    public void construir() {
        int numEstados = automata.estados.size();
        matriz = new int[numEstados][COLUMNAS.size()];

        for (int[] fila : matriz) {
            Arrays.fill(fila, Tokens.ERROR); // -100 por defecto = sin transición
        }

        for (Automata.Estado e : automata.estados) {
            for (var entrada : e.transiciones.entrySet()) {
                int col = COLUMNAS.indexOf(entrada.getKey());
                if (col == -1) {
                    throw new IllegalStateException(
                        "Clase de caracter '" + entrada.getKey() +
                        "' no está registrada en COLUMNAS. Agrégala en MatrizTransicion.");
                }
                matriz[e.id][col] = entrada.getValue();
            }
        }
    }

    /** Da el siguiente estado dado un estado actual y una clase de caracter. */
    public int siguienteEstado(int estadoActual, String claseCaracter) {
        int col = COLUMNAS.indexOf(claseCaracter);
        if (col == -1) return Tokens.ERROR;
        return matriz[estadoActual][col];
    }

    /**
     * Clasifica un caracter leído del archivo fuente en una de las
     * columnas de la matriz. Este método es el punto central donde
     * el equipo debe ir agregando casos según complete el autómata.
     */
    public static String claseDe(char c) {
        if (Character.isLetter(c)) return "letra";
        if (Character.isDigit(c)) return "digito";
        if (c == '<') return "menor";
        if (c == '>') return "mayor";
        if (c == '=') return "igual";
        if (c == ':') return "dospuntos";
        if (c == '.') return "punto";
        if (c == '"') return "comilla";
        if (c == '\\') return "barra";
        if (c == '&') return "amper";
        if (c == '|') return "pipe";
        return "otros";
    }

    /**
     * Genera la representación en texto de la matriz, pensada para
     * el archivo de salida "matriz_transiciones.txt". Formato similar
     * al ejemplo del PDF: encabezado con las columnas y una fila por
     * estado.
     */
    public String exportarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== MATRIZ DE TRANSICIONES =====\n\n");

        sb.append(String.format("%-8s", "Estado"));
        for (String col : COLUMNAS) {
            sb.append(String.format("%-10s", col));
        }
        sb.append("\n");

        for (int i = 0; i < matriz.length; i++) {
            sb.append(String.format("%-8d", i));
            for (int j = 0; j < COLUMNAS.size(); j++) {
                sb.append(String.format("%-10d", matriz[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
