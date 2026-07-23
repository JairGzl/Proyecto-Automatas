import java.util.ArrayList;
import java.util.List;

/**
 * CLASE 4: TABLA DE TOKENS
 */
public class TablaTokens {

    // Representa un registro de la tabla de tokens
    public static class Entrada {
        public final String cadena;
        public final int token;
        public final int posicion;
        public final int linea;

        public Entrada(String cadena, int token, int posicion, int linea) {
            this.cadena = cadena;
            this.token = token;
            this.posicion = posicion;
            this.linea = linea;
        }
    }

    private final List<Entrada> entradas = new ArrayList<>();

    /**
     * Agrega un token a la tabla.
     */
    public void agregar(String cadena, int token, int linea) {
        entradas.add(new Entrada(cadena, token, entradas.size() + 1, linea));
    }

    /**
     * Devuelve la lista de entradas.
     */
    public List<Entrada> getEntradas() {
        return entradas;
    }

    

    /**
     * Genera la tabla de tokens en formato de texto.
     */
    public String exportarTexto() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n================ CARACTERES QUE NO GENERAN TOKEN ================\n\n");
        for (String[] c : Tokens.NO_GENERAN_TOKEN) {
            sb.append(String.format("%-6s %s%n", c[0], c[1]));
        }

        sb.append("================ TABLA DE TOKENS ================\n\n");

        sb.append(String.format("%-20s %-25s %-10s %-10s%n",
                "CADENA", "TOKEN", "POSICIÓN", "LÍNEA"));

        sb.append("---------------------------------------------------------------\n");

        for (Entrada e : entradas) {

            sb.append(String.format("%-20s %-25s %-10d %-10d%n",
                    e.cadena,
                    Tokens.nombre(e.token) + " (" + e.token + ")",
                    e.posicion,
                    e.linea));
        }

        return sb.toString();
    }
}