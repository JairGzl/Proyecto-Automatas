import java.util.HashMap;
import java.util.Map;

/**
 * CLASE 1: TOKENS
 */
public class Tokens {

    
    // 1. PALABRAS RESERVADAS  -> códigos -1 a -20
    
    public static final Map<String, Integer> RESERVADAS = new HashMap<>();
    static {
        RESERVADAS.put("PROG",     -1);
        RESERVADAS.put("VAR",      -2);
        RESERVADAS.put("PROC",     -3);
        RESERVADAS.put("INICIO",   -4);
        RESERVADAS.put("FIN",      -5);
        RESERVADAS.put("ENTERO",   -6);
        RESERVADAS.put("REAL",     -7);
        RESERVADAS.put("STRING",   -8);
        RESERVADAS.put("LIMPIAR",  -9);
        RESERVADAS.put("VEXY",     -10);
        RESERVADAS.put("LEER",     -11);
        RESERVADAS.put("ESCRIBIR", -12);
        RESERVADAS.put("REPITE",   -13);
        RESERVADAS.put("HASTA",    -14);
        RESERVADAS.put("MIENTRAS", -15);
        RESERVADAS.put("SI",       -16);
        RESERVADAS.put("SINO",     -17);
        RESERVADAS.put("EJECUTA",  -18);
        RESERVADAS.put("AND",      -19);
        RESERVADAS.put("OR",       -20);
    }

    
    // 2. OPERADORES ARITMÉTICOS -> códigos -31 a -34

    public static final int SUMA          = -31; // +
    public static final int RESTA         = -32; // -
    public static final int MULTIPLICA    = -33; // *
    public static final int DIVIDE        = -34; // /

    
    // 3. OPERADORES RELACIONALES -> códigos -41 a -46
    
    public static final int MENOR         = -41; // <
    public static final int MENOR_IGUAL   = -42; // <=
    public static final int DISTINTO      = -43; // <>
    public static final int MAYOR         = -44; // >
    public static final int MAYOR_IGUAL   = -45; // >=
    public static final int IGUAL         = -46; // =

    
    // 4. OPERADORES LÓGICOS -> códigos -51 a -53
    
    public static final int AND_LOGICO    = -51; // &&
    public static final int OR_LOGICO     = -52; // || (confirmar con el profe)
    public static final int NOT_LOGICO    = -53; // !

    
    // 5. IDENTIFICADORES Y CONSTANTES -> códigos -61 a -64
    
    public static final int IDENTIFICADOR = -61;
    public static final int CTE_ENTERA    = -62;
    public static final int CTE_REAL      = -63;
    public static final int CTE_STRING    = -64;

    
    // 6. CARACTERES ESPECIALES QUE SÍ GENERAN TOKEN -> -81 a -88
    
    public static final int PYCOMA        = -81; // ;
    public static final int CORCHETE_ABRE = -82; // [
    public static final int CORCHETE_CIERRA = -83; // ]
    public static final int COMA          = -84; // ,
    public static final int DOSPUNTOS     = -85; // :
    public static final int PARENTESIS_ABRE  = -86; // (
    public static final int PARENTESIS_CIERRA = -87; // )
    public static final int ASIGNACION    = -88; // :=

    
    // 7. CÓDIGO DE ERROR
    
    public static final int ERROR = -100;

    /**
     * Regresa el código de una palabra reservada, o null si la cadena
     * no es una palabra reservada 
     */
    public static Integer codigoReservada(String palabra) {
        return RESERVADAS.get(palabra.toUpperCase());
    }

    /**
     * Imprimir el nombre de un token a partir
     * de su código, útil 
     */
    public static String nombre(int codigo) {
        for (Map.Entry<String, Integer> e : RESERVADAS.entrySet()) {
            if (e.getValue() == codigo) return e.getKey();
        }
        switch (codigo) {
            case SUMA: return "+";
            case RESTA: return "-";
            case MULTIPLICA: return "*";
            case DIVIDE: return "/";
            case MENOR: return "<";
            case MENOR_IGUAL: return "<=";
            case DISTINTO: return "<>";
            case MAYOR: return ">";
            case MAYOR_IGUAL: return ">=";
            case IGUAL: return "=";
            case AND_LOGICO: return "&&";
            case OR_LOGICO: return "||";
            case NOT_LOGICO: return "!";
            case IDENTIFICADOR: return "Identificador";
            case CTE_ENTERA: return "Cte_entera";
            case CTE_REAL: return "Cte_real";
            case CTE_STRING: return "Cte_string";
            case PYCOMA: return ";";
            case CORCHETE_ABRE: return "[";
            case CORCHETE_CIERRA: return "]";
            case COMA: return ",";
            case DOSPUNTOS: return ":";
            case PARENTESIS_ABRE: return "(";
            case PARENTESIS_CIERRA: return ")";
            case ASIGNACION: return ":=";
            case ERROR: return "ERROR";
            default: return "DESCONOCIDO(" + codigo + ")";
        }
    }
}