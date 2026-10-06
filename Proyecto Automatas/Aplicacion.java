import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

        // 1. Espacios en blanco y saltos de línea (No generan token)
        if (c == '\n') { 
            linea++; 
            i++; 
            continue; 
        }
        if (c == ' ' || c == '\t' || c == '\r') { 
            i++; 
            continue; 
        }

        // 2. Comentarios de línea (//) - Se omiten completamente, no generan token
        if (c == '/' && i + 1 < n && codigo.charAt(i + 1) == '/') {
            i += 2;
            while (i < n && codigo.charAt(i) != '\n') {
                i++;
            }
            continue; // El \n lo procesará la siguiente iteración
        }

        // 3. Cadenas de texto ("...") - Excluye las comillas del lexema final
    if (c == '"') {
    int inicioContenido = i + 1; // El texto real empieza después de la primera comilla
    i++; // Avanzamos el puntero para "consumir" la comilla de apertura
    
    boolean cerrado = false;
    while (i < n && codigo.charAt(i) != '\n') {
        if (codigo.charAt(i) == '"') {
            cerrado = true;
            break; // 'i' queda apuntando exactamente a la comilla de cierre
        }
        i++;
    }
    
    if (cerrado) {
        // Extraemos SOLO el contenido, sin las comillas (desde inicioContenido hasta i)
        String lexema = codigo.substring(inicioContenido, i);
        tabla.agregar(lexema, Tokens.CTE_STRING, linea);
        
        i++; // Avanzamos 'i' una vez más para saltar la comilla de cierre y continuar el escaneo
    } else {
        // Error: cadena no cerrada. 
        // Se reporta incluyendo la comilla inicial para que el profesor vea claramente dónde inició el error
        String lexemaError = codigo.substring(inicioContenido - 1, i);
        tabla.agregar(lexemaError, Tokens.ERROR, linea);
    }
    continue;
}

        // 4. Números y combinaciones inválidas (.5, 5., 71x, 4.5.6)
        // Esto evita que el autómata separe "71" y "x", o "." y "5"
        if (Character.isDigit(c) || c == '.') {
            int inicioNum = i;
            int puntos = 0;
            boolean tieneLetras = false;

            while (i < n) {
                char actual = codigo.charAt(i);
                if (Character.isDigit(actual)) { 
                    i++; 
                } else if (actual == '.') { 
                    puntos++; 
                    i++; 
                } else if (Character.isLetter(actual)) { 
                    tieneLetras = true; 
                    i++; 
                } else { 
                    break; // Fin del posible número
                }
            }

            String lexemaNum = codigo.substring(inicioNum, i);

            // Reglas de validación estricta para constantes numéricas
            if (tieneLetras || puntos > 1 || lexemaNum.equals(".") || lexemaNum.startsWith(".") || lexemaNum.endsWith(".")) {
                // Casos: "71x", "4.5.6", ".5", "5.", o un punto aislado "."
                tabla.agregar(lexemaNum, Tokens.ERROR, linea);
            } else if (puntos == 1) {
                // Caso válido: "4.5"
                tabla.agregar(lexemaNum, Tokens.CTE_REAL, linea);
            } else {
                // Caso válido: "71", "5"
                tabla.agregar(lexemaNum, Tokens.CTE_ENTERA, linea);
            }
            continue;
        }

        // 5. Operadores lógicos de dos caracteres (&&, ||)
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

        // 6. Símbolos simples (+, -, *, /, ;, etc.)
        // Asegúrate de que '.' NO esté en SIMBOLOS_SIMPLES para que el paso 4 lo controle
        if (SIMBOLOS_SIMPLES.containsKey(c)) {
            tabla.agregar(String.valueOf(c), SIMBOLOS_SIMPLES.get(c), linea);
            i++;
            continue;
        }

        // 7. Identificadores, palabras reservadas y fallback del autómata
        // Aquí caerán casos como "x71" (empieza con letra, el paso 4 lo ignora)
        ResultadoEscaneo r = escanearConAutomata(codigo, i);
        if (r.reconocido) {
            tabla.agregar(r.lexema, r.token, linea);
            i = r.nuevaPosicion;
        } else {
            // Carácter totalmente desconocido para el autómata
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
