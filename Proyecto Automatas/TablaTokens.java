import java.util.ArrayList;
import java.util.List;

/**
 * CLASE 4: TABLA DE TOKENS
 * ---------------------------------------------------------------------
 * RESPONSABLE SUGERIDO: Integrante 4
 *
 * Qué debe hacer esta clase:
 *   - Guardar cada "renglón" de la tabla final que pide el proyecto:
 *         (cadena, token, posición_en_tabla, número_de_línea)
 *   - IMPORTANTE (según la nota del PDF): NO hay tabla de tokens y
 *     tabla de errores por separado. Las cadenas inválidas también
 *     se agregan aquí, usando Tokens.ERROR (-100) como código.
 *   - El orden de inserción importa: debe ser el mismo orden en que
 *     las cadenas aparecen en el archivo fuente, porque así se va a
 *     revisar.
 *   - Exportar la tabla final al archivo de texto de salida.
 *
 * Qué falta por hacer:
 *   - Nada estructural; esta clase ya está completa. Lo que sí puede
 *     ajustarse es el formato exacto de impresión si el profesor pide
 *     un formato distinto (por ejemplo separado por comas en vez de
 *     alineado en columnas).
 *      hola
 */

public class TablaTokens {

    /** Un renglón de la tabla de tokens. */
    public static class Entrada {
        public final String cadena;
        public final int token;
        public final int posicion; // posición dentro de esta misma tabla (1, 2, 3...)
        public final int linea;    // número de línea en el archivo fuente

        public Entrada(String cadena, int token, int posicion, int linea) {
            this.cadena = cadena;
            this.token = token;
            this.posicion = posicion;
            this.linea = linea;
        }
    }

    private final List<Entrada> entradas = new ArrayList<>();

    /**
     * Agrega un nuevo renglón a la tabla. La posición se asigna
     * automáticamente según cuántas entradas ya existen (1-indexed).
     */
    public void agregar(String cadena, int token, int linea) {
        int posicion = entradas.size() + 1;
        entradas.add(new Entrada(cadena, token, posicion, linea));
    }

    public List<Entrada> getEntradas() {
        return entradas;
    }

    /**
     * Genera la representación en texto de la tabla completa, en el
     * formato (cadena, token, posición, línea), pensada para el
     * archivo de salida "tabla_tokens.txt".
     */
    public String exportarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== TABLA DE TOKENS =====\n\n");
        for (Entrada e : entradas) {
            sb.append("(")
              .append(e.cadena).append(", ")
              .append(Tokens.nombre(e.token)).append(" ").append(e.token).append(", ")
              .append(e.posicion).append(", ")
              .append(e.linea)
              .append(")\n");
        }
        return sb.toString();
    }
}
