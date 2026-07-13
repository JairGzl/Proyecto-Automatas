import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CLASE 2: AUTOMATA
 * ---------------------------------------------------------------------
 * RESPONSABLE SUGERIDO: Integrante 2
 *
 * Qué debe hacer esta clase:
 *   - Definir el autómata finito del análisis léxico completo: sus
 *     ESTADOS (nodos) y sus TRANSICIONES (arcos) entre estados según
 *     la "clase de carácter" que se lea (letra, dígito, comilla, etc).
 *   - Cada Estado sabe si es de aceptación (final) y, si lo es, a qué
 *     token corresponde (usa las constantes de Tokens.java).
 *   - Esta clase es la que "diagrama" en código lo mismo que en clase
 *     se dibuja a mano con círculos y flechas.
 *   - MatrizTransicion.java tomará estos mismos estados/transiciones
 *     para construir la tabla numérica (la matriz de transiciones).
 *
 * Qué falta por hacer (trabajo del equipo):
 *   - Ya se dejó armado, a modo de EJEMPLO funcional, el sub-autómata
 *     para: identificadores / palabras reservadas, operadores de uno
 *     o dos caracteres (<, <=, etc), y símbolos especiales simples.
 *   - FALTA agregar los estados para: constantes reales, constantes
 *     string (con el caso de comilla escapada \"), y comentarios (\\).
 *     Cada quien puede agregar sus estados nuevos en el método
 *     construirAutomata() siguiendo el mismo patrón.
 *   - Las "clases de carácter" (columnas) usadas aquí son:
 *     "letra", "digito", "punto", "comilla", "barra", "dospuntos",
 *     "amper", "pipe", "otros". Pueden agregar más si su automata
 *     lo requiere, pero deben avisar al responsable de MatrizTransicion
 *     para que la matriz las incluya también.
 */
public class Automata {

    /** Representa un solo estado (nodo) del autómata. */
    public static class Estado {
        public final int id;
        public String descripcion;
        public boolean esFinal;
        public Integer tokenSiFinal; // null si no es final o si requiere retroceso especial

        // transiciones: clase de caracter -> id del estado destino
        public Map<String, Integer> transiciones = new LinkedHashMap<>();

        public Estado(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public void addTransicion(String claseCaracter, int destino) {
            transiciones.put(claseCaracter, destino);
        }
    }

    public List<Estado> estados = new ArrayList<>();

    public Estado nuevoEstado(String descripcion) {
        Estado e = new Estado(estados.size(), descripcion);
        estados.add(e);
        return e;
    }

    public Estado get(int id) {
        return estados.get(id);
    }

    /**
     * Construye el autómata del lenguaje. Esta es la parte que el
     * equipo debe completar/ajustar según el diseño final que hagan
     * a mano en papel antes de programar (paso 3 y 4 de la metodología
     * del PDF).
     *
     * Ya viene armado un ejemplo funcional que cubre:
     *   - Estado 0: estado inicial
     *   - Identificadores / palabras reservadas (letra + letras/dígitos)
     *   - Constantes enteras (dígito+)
     *   - Operadores relacionales de 1 o 2 caracteres (<, <=, <>, etc)
     *   - Símbolos especiales de un solo caracter ( ; , [ ] ( ) )
     *   - ":" y ":=" 
     *
     * TODO equipo: agregar aquí los estados para:
     *   - Constantes reales (con el punto decimal)
     *   - Constantes string (con manejo de \" escapado)
     *   - Comentarios (\\ hasta fin de línea, no genera token)
     */
    public void construirAutomata() {
        Estado e0 = nuevoEstado("Estado inicial");

        // ---- Identificadores / palabras reservadas ----
        Estado e1 = nuevoEstado("Leyendo identificador/reservada");
        e0.addTransicion("letra", e1.id);
        e1.addTransicion("letra", e1.id);
        e1.addTransicion("digito", e1.id);
        // el cierre (aceptación) se resuelve en tiempo de ejecución:
        // si la palabra está en Tokens.RESERVADAS usamos ese código,
        // si no, es Tokens.IDENTIFICADOR. Por eso aquí no se marca
        // tokenSiFinal fijo; el analizador consulta Tokens al llegar
        // a un delimitador.
        e1.esFinal = true;
        e1.tokenSiFinal = Tokens.IDENTIFICADOR; // valor por defecto

        // ---- Constantes enteras ----
        Estado e2 = nuevoEstado("Leyendo constante entera");
        e0.addTransicion("digito", e2.id);
        e2.addTransicion("digito", e2.id);
        e2.esFinal = true;
        e2.tokenSiFinal = Tokens.CTE_ENTERA;

        // TODO equipo (constantes reales): agregar aquí, por ejemplo,
        // un estado e2b para cuando después de dígitos aparece un
        // "punto" y luego se exige al menos un dígito más (regla:
        // "no pueden iniciar y terminar con punto decimal").
        // e2.addTransicion("punto", e2b.id);
        // e2b.addTransicion("digito", e2c.id);
        // e2c.addTransicion("digito", e2c.id);
        // e2c.esFinal = true; e2c.tokenSiFinal = Tokens.CTE_REAL;

        // ---- Operador relacional que empieza con "<" ----
        Estado e3 = nuevoEstado("Leyo '<'");
        e0.addTransicion("menor", e3.id);
        e3.esFinal = true;
        e3.tokenSiFinal = Tokens.MENOR;

        Estado e4 = nuevoEstado("Leyo '<='");
        e3.addTransicion("igual", e4.id);
        e4.esFinal = true;
        e4.tokenSiFinal = Tokens.MENOR_IGUAL;

        Estado e5 = nuevoEstado("Leyo '<>'");
        e3.addTransicion("mayor", e5.id);
        e5.esFinal = true;
        e5.tokenSiFinal = Tokens.DISTINTO;

        // ---- Operador relacional que empieza con ">" ----
        Estado e6 = nuevoEstado("Leyo '>'");
        e0.addTransicion("mayor", e6.id);
        e6.esFinal = true;
        e6.tokenSiFinal = Tokens.MAYOR;

        Estado e7 = nuevoEstado("Leyo '>='");
        e6.addTransicion("igual", e7.id);
        e7.esFinal = true;
        e7.tokenSiFinal = Tokens.MAYOR_IGUAL;

        // ---- ":" y ":=" ----
        Estado e8 = nuevoEstado("Leyo ':'");
        e0.addTransicion("dospuntos", e8.id);
        e8.esFinal = true;
        e8.tokenSiFinal = Tokens.DOSPUNTOS;

        Estado e9 = nuevoEstado("Leyo ':='");
        e8.addTransicion("igual", e9.id);
        e9.esFinal = true;
        e9.tokenSiFinal = Tokens.ASIGNACION;

        // ---- Símbolos especiales de un solo caracter ----
        // (se resuelven directo, sin necesitar un estado propio,
        //  ver Aplicacion.java -> metodo escanear())

        // TODO equipo (strings): agregar estados para comillas,
        // manejo del caso "\\\"" (barra invertida + comilla dentro
        // del string), y cierre obligatorio en la misma línea.

        // TODO equipo (comentarios): agregar estado para "\\" que
        // consuma todo hasta fin de línea sin generar token.
    }

    /**
     * Genera una representación en texto plano del autómata,
     * pensada para volcarse en el archivo de salida "automata.txt".
     */
    public String exportarTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== AUTOMATA (estados y transiciones) =====\n\n");
        for (Estado e : estados) {
            sb.append("Estado ").append(e.id)
              .append(" - ").append(e.descripcion);
            if (e.esFinal) {
                sb.append("  [FINAL -> token: ")
                  .append(Tokens.nombre(e.tokenSiFinal)).append("]");
            }
            sb.append("\n");
            for (Map.Entry<String, Integer> t : e.transiciones.entrySet()) {
                sb.append("    con '").append(t.getKey())
                  .append("' -> Estado ").append(t.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}
