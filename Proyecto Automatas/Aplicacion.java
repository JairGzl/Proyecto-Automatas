import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * CLASE 5: APLICACION (main)
 * ---------------------------------------------------------------------
 * RESPONSABLE SUGERIDO: Integrante 4 (o el equipo completo en conjunto,
 * ya que aquí se integra el trabajo de todos)
 *
 * Qué debe hacer esta clase:
 *   1. Leer el archivo .txt de entrada (el código fuente a analizar).
 *   2. Recorrer el texto caracter por caracter, usando la Matriz de
 *      Transiciones (que a su vez viene del Automata) para reconocer
 *      cada token.
 *   3. Ir llenando la TablaTokens con cada cadena reconocida (o marcada
 *      como error si no corresponde a ningún token válido).
 *   4. Al terminar, escribir TRES archivos de salida:
 *        - automata.txt            (Automata.exportarTexto())
 *        - matriz_transiciones.txt (MatrizTransicion.exportarTexto())
 *        - tabla_tokens.txt        (TablaTokens.exportarTexto())
 *
 * Cómo correrlo:
 *   javac -d out src/*.java
 *   java -cp out Aplicacion entradas/entrada.txt salida
 *
 *   (si no se dan argumentos, usa "entradas/entrada.txt" y "salida/"
 *    por defecto)
 *
 * Qué falta por hacer (trabajo del equipo):
 *   - El método escanear() ya reconoce: identificadores, palabras
 *     reservadas, constantes enteras, operadores relacionales,
 *     operadores lógicos completos (&&, ||, !), operadores
 *     aritméticos y símbolos especiales de un caracter, y ":"/":=" .
 *   - FALTA integrar, una vez que Automata.java y MatrizTransicion.java
 *     tengan los estados de constantes reales, strings y comentarios,
 *     el reconocimiento de esos 3 casos aquí (están marcados con TODO
 *     más abajo). Mientras tanto, esas cadenas caerán como ERROR, lo
 *     cual es esperado y no rompe el programa.
 */
public class Aplicacion {

    // Tokens de un solo caracter que no requieren pasar por el autómata
    private static final Map<Character, Integer> SIMBOLOS_SIMPLES = new HashMap<>();
    static {
        SIMBOLOS_SIMPLES.put(';', Tokens.PYCOMA);
        SIMBOLOS_SIMPLES.put('[', Tokens.CORCHETE_ABRE);
        SIMBOLOS_SIMPLES.put(']', Tokens.CORCHETE_CIERRA);
        SIMBOLOS_SIMPLES.put(',', Tokens.COMA);
        SIMBOLOS_SIMPLES.put('(', Tokens.PARENTESIS_ABRE);
        SIMBOLOS_SIMPLES.put(')', Tokens.PARENTESIS_CIERRA);
        SIMBOLOS_SIMPLES.put('+', Tokens.SUMA);
        SIMBOLOS_SIMPLES.put('-', Tokens.RESTA);
        SIMBOLOS_SIMPLES.put('*', Tokens.MULTIPLICA);
        SIMBOLOS_SIMPLES.put('/', Tokens.DIVIDE);
        SIMBOLOS_SIMPLES.put('!', Tokens.NOT_LOGICO);
    }

    private final Automata automata;
    private final MatrizTransicion matriz;
    private final TablaTokens tabla = new TablaTokens();

    public Aplicacion(Automata automata, MatrizTransicion matriz) {
        this.automata = automata;
        this.matriz = matriz;
    }

    public static void main(String[] args) throws IOException {
        String rutaEntrada = args.length > 0 ? args[0] : "entradas/entrada.txt";
        String carpetaSalida = args.length > 1 ? args[1] : "salida";

        // 1. Construir el automata y su matriz (clases 2 y 3)
        Automata automata = new Automata();
        automata.construirAutomata();

        MatrizTransicion matriz = new MatrizTransicion(automata);
        matriz.construir();

        // 2. Correr el analisis lexico sobre el archivo de entrada
        Aplicacion app = new Aplicacion(automata, matriz);
        String codigoFuente = Files.readString(Path.of(rutaEntrada), StandardCharsets.UTF_8);
        app.escanear(codigoFuente);

        // 3. Escribir los 3 archivos de salida
        Files.createDirectories(Path.of(carpetaSalida));
        Files.writeString(Path.of(carpetaSalida, "automata.txt"), automata.exportarTexto());
        Files.writeString(Path.of(carpetaSalida, "matriz_transiciones.txt"), matriz.exportarTexto());
        Files.writeString(Path.of(carpetaSalida, "tabla_tokens.txt"), app.tabla.exportarTexto());

        System.out.println("Listo. Archivos generados en la carpeta: " + carpetaSalida);
    }

    /**
     * Recorre el código fuente caracter por caracter y va llenando
     * la tabla de tokens.
     */
    public void escanear(String codigo) {
        int i = 0;
        int linea = 1;
        int n = codigo.length();

        while (i < n) {
            char c = codigo.charAt(i);

            // --- Saltos de linea y espacios en blanco: no generan token ---
            if (c == '\n') {
                linea++;
                i++;
                continue;
            }
            if (c == ' ' || c == '\t' || c == '\r') {
                i++;
                continue;
            }

            // TODO equipo (comentarios): si c == '\\' y el siguiente
            // caracter tambien es '\\', consumir hasta fin de linea
            // sin generar token. Agregarlo aqui ANTES del caso de
            // simbolos simples, ya que "\\" no esta en SIMBOLOS_SIMPLES.

            // TODO equipo (strings): si c == '"', consumir hasta la
            // siguiente comilla no escapada (cuidado con \" dentro del
            // string) y generar un token Tokens.CTE_STRING. Si el
            // string no cierra en la misma linea, es un error.

            // --- Operadores logicos de dos caracteres: && y || ---
            if (c == '&' && i + 1 < n && codigo.charAt(i + 1) == '&') {
                tabla.agregar("&&", Tokens.AND_LOGICO, linea);
                i += 2;
                continue;
            }
            if (c == '|' && i + 1 < n && codigo.charAt(i + 1) == '|') {
                tabla.agregar("||", Tokens.OR_LOGICO, linea);
                i += 2;
                continue;
            }

            // --- Simbolos de un solo caracter ---
            if (SIMBOLOS_SIMPLES.containsKey(c)) {
                tabla.agregar(String.valueOf(c), SIMBOLOS_SIMPLES.get(c), linea);
                i++;
                continue;
            }

            // --- Todo lo demas se resuelve recorriendo el automata ---
            ResultadoEscaneo r = escanearConAutomata(codigo, i);
            if (r.reconocido) {
                tabla.agregar(r.lexema, r.token, linea);
                i = r.nuevaPosicion;
            } else {
                // No hubo ningun estado final valido: se marca como error
                // de un solo caracter y se avanza para no ciclar infinito.
                tabla.agregar(String.valueOf(c), Tokens.ERROR, linea);
                i++;
            }
        }
    }

    /** Pequeño contenedor para el resultado de recorrer el automata. */
    private static class ResultadoEscaneo {
        boolean reconocido;
        String lexema;
        int token;
        int nuevaPosicion;
    }

    /**
     * Recorre el automata (via la matriz de transiciones) empezando
     * en la posicion "inicio" del texto, aplicando la regla de
     * "maxima cadena reconocida" (longest match): si en algun punto
     * ya no hay transicion valida, se regresa al ultimo estado final
     * que se haya visto.
     */
    private ResultadoEscaneo escanearConAutomata(String codigo, int inicio) {
        int n = codigo.length();
        int estado = 0;
        int j = inicio;

        int ultimoEstadoFinal = -1;
        int ultimaPosicionFinal = -1;

        while (j < n) {
            char c = codigo.charAt(j);
            if (c == '\n' || c == ' ' || c == '\t' || c == '\r') break;

            String clase = MatrizTransicion.claseDe(c);
            int siguiente = matriz.siguienteEstado(estado, clase);
            if (siguiente == Tokens.ERROR) break;

            estado = siguiente;
            j++;

            if (automata.get(estado).esFinal) {
                ultimoEstadoFinal = estado;
                ultimaPosicionFinal = j;
            }
        }

        ResultadoEscaneo r = new ResultadoEscaneo();
        if (ultimoEstadoFinal == -1) {
            r.reconocido = false;
            return r;
        }

        String lexema = codigo.substring(inicio, ultimaPosicionFinal);
        int token = automata.get(ultimoEstadoFinal).tokenSiFinal;

        // Si el token es "Identificador", verificar si en realidad es
        // una palabra reservada (PROG, VAR, SI, etc.)
        if (token == Tokens.IDENTIFICADOR) {
            Integer reservada = Tokens.codigoReservada(lexema);
            if (reservada != null) token = reservada;
        }

        r.reconocido = true;
        r.lexema = lexema;
        r.token = token;
        r.nuevaPosicion = ultimaPosicionFinal;
        return r;
    }
}
