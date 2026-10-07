import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Aplicacion {

    private static final Map<Character, Integer> SIMBOLOS_SIMPLES = new HashMap<>();
    static {
        SIMBOLOS_SIMPLES.put(';', Tokens.PYCOMA);
        SIMBOLOS_SIMPLES.put('[', Tokens.CORCHETE_ABRE);
        SIMBOLOS_SIMPLES.put(']', Tokens.CORCHETE_CIERRA);
        SIMBOLOS_SIMPLES.put(',', Tokens.COMA);
        SIMBOLOS_SIMPLES.put(':', Tokens.DOSPUNTOS);
        SIMBOLOS_SIMPLES.put('(', Tokens.PARENTESIS_ABRE);
        SIMBOLOS_SIMPLES.put(')', Tokens.PARENTESIS_CIERRA);
        SIMBOLOS_SIMPLES.put('{', Tokens.LLAVE_ABRE);
        SIMBOLOS_SIMPLES.put('}', Tokens.LLAVE_CIERRA);
        SIMBOLOS_SIMPLES.put('*', Tokens.MULTIPLICA);
        SIMBOLOS_SIMPLES.put('%', Tokens.PORCENTAJE);
        SIMBOLOS_SIMPLES.put('/', Tokens.DIVIDE); // ojo: "//" (comentario) se revisa ANTES que esto
    }

    private static final BigInteger ENTERO_MIN = BigInteger.valueOf(-32768);
    private static final BigInteger ENTERO_MAX = BigInteger.valueOf(32767);

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

        Automata automata = new Automata();
        automata.construirAutomata();

        MatrizTransicion matriz = new MatrizTransicion(automata);
        matriz.construir();

        Aplicacion app = new Aplicacion(automata, matriz);
        String codigoFuente = Files.readString(Path.of(rutaEntrada), StandardCharsets.UTF_8);
        app.escanear(codigoFuente);

        Files.createDirectories(Path.of(carpetaSalida));
        Files.writeString(Path.of(carpetaSalida, "automata.txt"), automata.exportarTexto());
        Files.writeString(Path.of(carpetaSalida, "matriz_transiciones.txt"), matriz.exportarTexto());
        Files.writeString(Path.of(carpetaSalida, "tabla_tokens.txt"), app.tabla.exportarTexto());

        System.out.println("Listo. Archivos generados en la carpeta: " + carpetaSalida);
    }

    public void escanear(String codigo) {
        int i = 0;
        int linea = 1;
        int n = codigo.length();

        while (i < n) {
            char c = codigo.charAt(i);

            if (c == '\n') { linea++; i++; continue; }
            if (c == ' ' || c == '\t' || c == '\r') { i++; continue; }

            if (c == '/' && i + 1 < n && codigo.charAt(i + 1) == '/') {
                while (i < n && codigo.charAt(i) != '\n') i++;
                continue;
            }

            if (c == '"') {
                ResultadoEscaneo r = escanearConAutomata(codigo, i);
                if (r.reconocido) {
                    String contenido = r.lexema.substring(1, r.lexema.length() - 1);
                    tabla.agregar(contenido, Tokens.CTE_STRING, linea);
                    i = r.nuevaPosicion;
                } else {
                    int finLinea = i;
                    while (finLinea < n && codigo.charAt(finLinea) != '\n') finLinea++;
                    tabla.agregar(codigo.substring(i, finLinea), Tokens.ERROR, linea);
                    i = finLinea;
                }
                continue;
            }

            if (SIMBOLOS_SIMPLES.containsKey(c)) {
                tabla.agregar(String.valueOf(c), SIMBOLOS_SIMPLES.get(c), linea);
                i++;
                continue;
            }

            ResultadoEscaneo r = escanearConAutomata(codigo, i);
            if (r.reconocido) {
                tabla.agregar(r.lexema, r.token, linea);
                i = r.nuevaPosicion;
            } else {
                tabla.agregar(String.valueOf(c), Tokens.ERROR, linea);
                i++;
            }
        }
    }

    private static class ResultadoEscaneo {
        boolean reconocido;
        String lexema;
        int token;
        int nuevaPosicion;
    }

    private ResultadoEscaneo escanearConAutomata(String codigo, int inicio) {
        int n = codigo.length();
        int estado = 0;
        int j = inicio;

        int ultimoEstadoFinal = -1;
        int ultimaPosicionFinal = -1;

        while (j < n) {
            char c = codigo.charAt(j);

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

        if (token == Tokens.IDENTIFICADOR) {
            Integer reservada = Tokens.codigoReservada(lexema);
            if (reservada != null) token = reservada;
        } else if (token == Tokens.CTE_ENTERA) {
            BigInteger valor = new BigInteger(lexema);
            if (valor.compareTo(ENTERO_MIN) < 0 || valor.compareTo(ENTERO_MAX) > 0) {
                token = Tokens.CTE_REAL;
            }
        }

        r.reconocido = true;
        r.lexema = lexema;
        r.token = token;
        r.nuevaPosicion = ultimaPosicionFinal;
        return r;
    }
}