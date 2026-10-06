import java.util.Arrays;
import java.util.List;

/**
 * CLASE 3: MATRIZ DE TRANSICION
 * ---------------------------------------------------------------------
 * Que hace esta clase:
 *   - Toma los Estados y transiciones definidos en Automata.java y
 *     los "aplana" en una tabla (matriz) de:
 *         filas    = estados
 *         columnas = clases de caracter (letra, digito, etc.)
 *         valor    = estado destino, o Tokens.ERROR (-1000) si no hay
 *                    transicion definida para esa combinacion.
 *   - Tambien clasifica cada caracter leido del archivo fuente en una
 *     de esas columnas (metodo claseDe(char)). Esa funcion es la que
 *     usa Aplicacion.java para "moverse" por la matriz mientras
 *     escanea el .txt de entrada.
 *
 * Columnas actuales (Proyecto Lexico, agosto 2026):
 *   - "dospuntos" y "barra" YA NO EXISTEN: el proyecto anterior las
 *     usaba para ":"/":=" y para escapes "\" dentro de strings, pero
 *     ninguna de las dos aplica al lenguaje de este PDF (el ":" es un
 *     simbolo suelto que se resuelve directo en Aplicacion.java, sin
 *     pasar por el automata, y los strings no manejan escapes).
 *   - "mas", "menos" y "exclamacion" son columnas NUEVAS: las necesitan
 *     los estados que Automata.java agrego para +, ++, +=, -, --, -=,
 *     ! y !=.
 *   - Si mas adelante se agrega algun estado nuevo en Automata.java
 *     que use una clase de caracter que no esta en COLUMNAS, el
 *     programa truena en tiempo de ejecucion con un mensaje claro
 *     (ver construir()) senalando que clase falta registrar aqui.
 */
public class MatrizTransicion {

    // Columnas de la matriz = clases de caracter reconocidas.
    // El orden no importa para el funcionamiento, solo para como se
    // ve impresa la matriz en matriz_transiciones.txt.
    public static final List<String> COLUMNAS = Arrays.asList(
            "letra", "digito", "menor", "mayor", "igual",
            "mas", "menos", "exclamacion",
            "punto", "comilla", "amper", "pipe", "otros"
    );

    private int[][] matriz; // matriz[estado][columna] = estado destino o Tokens.ERROR
    private final Automata automata;

    public MatrizTransicion(Automata automata) {
        this.automata = automata;
    }

    /**
     * Construye la matriz numerica a partir de los estados del
     * automata: recorre cada Estado y copia sus transiciones a la
     * fila correspondiente. Las casillas sin transicion definida se
     * quedan en Tokens.ERROR (sin transicion = no hay forma de seguir
     * leyendo desde ese estado con esa clase de caracter).
     */
    public void construir() {
        int numEstados = automata.estados.size();
        matriz = new int[numEstados][COLUMNAS.size()];

        for (int[] fila : matriz) {
            Arrays.fill(fila, Tokens.ERROR); // por defecto = sin transicion
        }

        for (Automata.Estado e : automata.estados) {
            for (var entrada : e.transiciones.entrySet()) {
                int col = COLUMNAS.indexOf(entrada.getKey());
                if (col == -1) {
                    // Si Automata.java usa una clase de caracter que no
                    // esta en COLUMNAS, es mas facil detectarlo aqui con
                    // un mensaje claro que descubrirlo despues con un
                    // resultado raro en la tabla de tokens.
                    throw new IllegalStateException(
                        "Clase de caracter '" + entrada.getKey() +
                        "' no esta registrada en COLUMNAS. Agregala en MatrizTransicion.");
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
     * Clasifica un caracter leido del archivo fuente en una de las
     * columnas de la matriz. Cualquier caracter que no entre en un
     * caso especifico (incluyendo ':', '\' y simbolos fuera del
     * lenguaje como '@' o '#') cae en "otros".
     */
    public static String claseDe(char c) {
        if (Character.isLetter(c)) return "letra";
        if (Character.isDigit(c)) return "digito";
        if (c == '<') return "menor";
        if (c == '>') return "mayor";
        if (c == '=') return "igual";
        if (c == '+') return "mas";
        if (c == '-') return "menos";
        if (c == '!') return "exclamacion";
        if (c == '.') return "punto";
        if (c == '"') return "comilla";
        if (c == '&') return "amper";
        if (c == '|') return "pipe";
        return "otros";
    }

    /**
     * Genera la representacion en texto de la matriz, pensada para
     * el archivo de salida "matriz_transiciones.txt". Formato similar
     * al ejemplo del PDF: encabezado con las columnas y una fila por
     * estado.
     */
    public String exportarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== MATRIZ DE TRANSICIONES =====\n\n");

        sb.append(String.format("%-8s", "Estado"));
        for (String col : COLUMNAS) {
            sb.append(String.format("%-13s", col));
        }
        sb.append("\n");

        for (int i = 0; i < matriz.length; i++) {
            sb.append(String.format("%-8d", i));
            for (int j = 0; j < COLUMNAS.size(); j++) {
                sb.append(String.format("%-13d", matriz[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}