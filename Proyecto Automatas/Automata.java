import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CLASE 2: AUTOMATA (Proyecto Lexico, agosto 2026)
 *
 * Representa el automata finito que reconoce todos los tokens del
 * lenguaje definido en el PDF del proyecto, EXCEPTO:
 *  - los simbolos de un solo caracter que no necesitan estado propio
 *    (; [ ] , : ( ) { } * % /), que se resuelven directo en
 *    Aplicacion.java
 *  - los comentarios "//", que tampoco generan token y se resuelven
 *    igual en Aplicacion.java (el automata nunca "ve" un comentario,
 *    porque Aplicacion salta el texto completo antes de llamarlo)
 */
public class Automata {

    /** Representa un solo estado (nodo) del automata. */
    public static class Estado {
        public final int id;
        public String descripcion;
        public boolean esFinal;
        public Integer tokenSiFinal; // null si no es final

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
     * Construye el automata completo del lenguaje: estado inicial,
     * identificadores/reservadas, constantes enteras y reales (con
     * punto al inicio o en medio), constantes string, operadores
     * relacionales (< <= > >=), aritmeticos (+ ++ += - -- -= =),
     * de igualdad (==), logicos (! != && ||).
     *
     * Las variables e0, e1, e2... son solo los NOMBRES que le damos a
     * cada estado en el codigo, en el orden en que los vamos creando;
     * no tienen un significado especial mas alla de identificar a
     * cada Estado. Lo importante es la cadena de .addTransicion(...)
     * que conecta unos con otros.
     */
    public void construirAutomata() {
        Estado e0 = nuevoEstado("Estado inicial");

        // ---- Identificadores / palabras reservadas ----
        // Letra, seguida de cualquier cantidad de letras o digitos.
        // Se queda como "Identificador" por defecto; Aplicacion.java
        // decide si en realidad es una palabra reservada (public,
        // class, if, etc.) consultando Tokens.codigoReservada().
        Estado e1 = nuevoEstado("Leyendo identificador/reservada");
        e0.addTransicion("letra", e1.id);
        e1.addTransicion("letra", e1.id);
        e1.addTransicion("digito", e1.id);
        e1.esFinal = true;
        e1.tokenSiFinal = Tokens.IDENTIFICADOR; // valor por defecto

        // ---- Constante entera: uno o mas digitos ----
        Estado e2 = nuevoEstado("Leyendo constante entera");
        e0.addTransicion("digito", e2.id);
        e2.addTransicion("digito", e2.id);
        e2.esFinal = true;
        e2.tokenSiFinal = Tokens.CTE_ENTERA;

        // ---- Constante real que empieza con digitos: 3.14 ----
        // Regla del PDF: "no pueden terminar con punto decimal", por
        // eso e3 (el estado justo despues del punto) NO es final:
        // obliga a que venga al menos un digito mas para aceptar.
        // "3." solo (sin digitos despues) se queda sin estado final,
        // y Aplicacion.java lo marca como ERROR.
        Estado e3 = nuevoEstado("Leyo punto despues de entero, falta al menos 1 digito");
        e2.addTransicion("punto", e3.id);

        Estado e4 = nuevoEstado("Leyendo parte decimal de constante real");
        e3.addTransicion("digito", e4.id);
        e4.addTransicion("digito", e4.id);
        e4.esFinal = true;
        e4.tokenSiFinal = Tokens.CTE_REAL;

        // ---- Constante real que empieza con punto: .5 ----
        // Regla del PDF: "pueden iniciar con punto decimal", por eso
        // esta rama existe aparte de la anterior. e5 tampoco es
        // final (un punto solo no es nada), y en cuanto llega un
        // digito se junta con el MISMO estado final e4 de arriba,
        // porque a partir de ahi es exactamente lo mismo: seguir
        // leyendo digitos de la parte decimal.
        Estado e5 = nuevoEstado("Leyo punto al inicio, falta al menos 1 digito");
        e0.addTransicion("punto", e5.id);
        e5.addTransicion("digito", e4.id);

        // ---- Operador relacional que empieza con "<" ----
        Estado e6 = nuevoEstado("Leyo '<'");
        e0.addTransicion("menor", e6.id);
        e6.esFinal = true;
        e6.tokenSiFinal = Tokens.MENOR;

        Estado e7 = nuevoEstado("Leyo '<='");
        e6.addTransicion("igual", e7.id);
        e7.esFinal = true;
        e7.tokenSiFinal = Tokens.MENOR_IGUAL;

        // ---- Operador relacional que empieza con ">" ----
        Estado e8 = nuevoEstado("Leyo '>'");
        e0.addTransicion("mayor", e8.id);
        e8.esFinal = true;
        e8.tokenSiFinal = Tokens.MAYOR;

        Estado e9 = nuevoEstado("Leyo '>='");
        e8.addTransicion("igual", e9.id);
        e9.esFinal = true;
        e9.tokenSiFinal = Tokens.MAYOR_IGUAL;

        // ---- Constantes String: "..." y deben cerrar en la misma linea ----
        // e10 = "dentro del string, leyendo caracteres". Acepta
        // CUALQUIER clase de caracter normal (letra, digito, simbolos)
        // EXCEPTO la comilla que cierra, por eso se le agregan
        // transiciones a si mismo para todas esas columnas. A
        // proposito NO se le agrega la clase "salto" (fin de linea):
        // si el string no se cerro antes del salto de linea, el
        // escaneo se queda sin estado final valido y Aplicacion.java
        // lo marca como ERROR, que es justo la regla del PDF.
        // Tampoco se maneja aqui el caracter de escape "\", porque el
        // PDF no lo pide; si tu maestro lo pide despues, aqui es
        // donde se agregaria un estado extra para esa rama.
        Estado e10 = nuevoEstado("Dentro de un string");
        e0.addTransicion("comilla", e10.id); // comilla inicial abre el string
        for (String clase : new String[]{
                "letra", "digito", "menor", "mayor", "igual",
                "punto", "amper", "pipe", "mas", "menos",
                "exclamacion", "otros"}) {
            e10.addTransicion(clase, e10.id);
        }

        Estado e11 = nuevoEstado("String cerrado");
        e10.addTransicion("comilla", e11.id); // comilla de cierre
        e11.esFinal = true;
        e11.tokenSiFinal = Tokens.CTE_STRING;

        // ---- Operador "+" y sus extensiones "++" "+=" ----
        // e12 ya es final por si solo (un "+" suelto es un token
        // valido, SUMA), pero TAMBIEN puede seguir extendiendose: si
        // justo despues viene otro "+" se vuelve "++", y si viene un
        // "=" se vuelve "+=". Por eso un estado puede ser final Y
        // seguir teniendo transiciones de salida al mismo tiempo.
        Estado e12 = nuevoEstado("Leyo '+'");
        e0.addTransicion("mas", e12.id);
        e12.esFinal = true;
        e12.tokenSiFinal = Tokens.SUMA;

        Estado e13 = nuevoEstado("Leyo '++'");
        e12.addTransicion("mas", e13.id);
        e13.esFinal = true;
        e13.tokenSiFinal = Tokens.SUMA_SUMA;

        Estado e14 = nuevoEstado("Leyo '+='");
        e12.addTransicion("igual", e14.id);
        e14.esFinal = true;
        e14.tokenSiFinal = Tokens.SUMA_IGUAL;

        // ---- Operador "-" y sus extensiones "--" "-=" ----
        // Mismo patron que "+", nada mas con "-".
        Estado e15 = nuevoEstado("Leyo '-'");
        e0.addTransicion("menos", e15.id);
        e15.esFinal = true;
        e15.tokenSiFinal = Tokens.RESTA;

        Estado e16 = nuevoEstado("Leyo '--'");
        e15.addTransicion("menos", e16.id);
        e16.esFinal = true;
        e16.tokenSiFinal = Tokens.RESTA_RESTA;

        Estado e17 = nuevoEstado("Leyo '-='");
        e15.addTransicion("igual", e17.id);
        e17.esFinal = true;
        e17.tokenSiFinal = Tokens.RESTA_IGUAL;

        // ---- Operador "=" y su extension "==" ----
        Estado e18 = nuevoEstado("Leyo '='");
        e0.addTransicion("igual", e18.id);
        e18.esFinal = true;
        e18.tokenSiFinal = Tokens.IGUAL;

        Estado e19 = nuevoEstado("Leyo '=='");
        e18.addTransicion("igual", e19.id);
        e19.esFinal = true;
        e19.tokenSiFinal = Tokens.IGUAL_IGUAL;

        // ---- Operador "!" y su extension "!=" ----
        Estado e20 = nuevoEstado("Leyo '!'");
        e0.addTransicion("exclamacion", e20.id);
        e20.esFinal = true;
        e20.tokenSiFinal = Tokens.NOT_LOGICO;

        Estado e21 = nuevoEstado("Leyo '!='");
        e20.addTransicion("igual", e21.id);
        e21.esFinal = true;
        e21.tokenSiFinal = Tokens.DIFERENTE;

        // ---- Operador logico "&&" ----
        // A diferencia de "+"/"-"/"="/"!", aqui el primer estado
        // (e22, "leyo un solo &") NO es final: tu PDF no tiene un
        // token para un "&" suelto, asi que si el siguiente caracter
        // no es otro "&", el escaneo se queda sin estado final valido
        // y se marca como ERROR. Solo al leer el segundo "&" (e23) se
        // vuelve un token valido, AND_LOGICO.
        Estado e22 = nuevoEstado("Leyo un '&', falta otro '&' (NO es final)");
        e0.addTransicion("amper", e22.id);

        Estado e23 = nuevoEstado("Leyo '&&'");
        e22.addTransicion("amper", e23.id);
        e23.esFinal = true;
        e23.tokenSiFinal = Tokens.AND_LOGICO;

        // ---- Operador logico "||" ----
        // Mismo patron que "&&": e24 no es final, solo e25 lo es.
        Estado e24 = nuevoEstado("Leyo un '|', falta otro '|' (NO es final)");
        e0.addTransicion("pipe", e24.id);

        Estado e25 = nuevoEstado("Leyo '||'");
        e24.addTransicion("pipe", e25.id);
        e25.esFinal = true;
        e25.tokenSiFinal = Tokens.OR_LOGICO;
    }

    /**
     * Genera una representacion en texto plano del automata, pensada
     * para volcarse en el archivo de salida "automata.txt". Recorre
     * cada estado y lista sus transiciones, y si es final, imprime a
     * que token corresponde (usando Tokens.nombre() para mostrar el
     * nombre legible en vez del numero).
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