import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * Construye el autómata completo del lenguaje: estado inicial,
     * identificadores/reservadas, constantes enteras y reales,
     * constantes string, operadores relacionales, y ":"/":=".
     * (Los comentarios "\\" se resuelven aparte, en Aplicacion.java,
     * porque no producen token — ver nota al final de este método).
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

        // ---- Constantes reales ----
        // Regla del proyecto: "no pueden iniciar y terminar con punto
        // decimal" -> por eso solo se llega aquí DESPUÉS de al menos
        // un dígito (desde e2), y el estado del punto (e10) NO es
        // final: obliga a que venga al menos un dígito más después
        // del punto para poder aceptar.
        Estado e10 = nuevoEstado("Leyo punto decimal, falta al menos 1 digito");
        e2.addTransicion("punto", e10.id);
        // e10 NO es final: "3." solo (sin dígitos después) es error/incompleto

        Estado e11 = nuevoEstado("Leyendo parte decimal de constante real");
        e10.addTransicion("digito", e11.id);
        e11.addTransicion("digito", e11.id);
        e11.esFinal = true;
        e11.tokenSiFinal = Tokens.CTE_REAL;

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

        // ---- Constantes String ----
        // e12 = "dentro del string, leyendo caracteres". Acepta
        // CUALQUIER clase de caracter (excepto la comilla que cierra
        // y la barra invertida que inicia un escape), por eso se
        // le agregan transiciones a sí mismo para todas las columnas
        // "normales". El salto de línea NO se agrega aquí porque
        // Aplicacion.java corta el escaneo al llegar a '\n' (por
        // regla del proyecto: "un string debe terminar en la misma
        // línea"), así que si no se cerró antes del salto de línea,
        // automáticamente queda sin estado final -> error.
        Estado e12 = nuevoEstado("Dentro de un string");
        e0.addTransicion("comilla", e12.id); // comilla inicial abre el string
        for (String clase : new String[]{
                "letra", "digito", "menor", "mayor", "igual",
                "dospuntos", "punto", "amper", "pipe", "otros"}) {
            e12.addTransicion(clase, e12.id);
        }

        Estado e13 = nuevoEstado("Leyo '\\' dentro del string (posible escape)");
        e12.addTransicion("barra", e13.id);
        // Después de la barra invertida, cualquier caracter (incluida
        // otra comilla escapada \") se toma como literal y se regresa
        // a seguir leyendo el string normalmente.
        for (String clase : new String[]{
                "letra", "digito", "menor", "mayor", "igual",
                "dospuntos", "punto", "amper", "pipe", "otros", "barra", "comilla"}) {
            e13.addTransicion(clase, e12.id);
        }

        Estado e14 = nuevoEstado("String cerrado");
        e12.addTransicion("comilla", e14.id); // comilla NO escapada -> cierra el string
        e14.esFinal = true;
        e14.tokenSiFinal = Tokens.CTE_STRING;

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