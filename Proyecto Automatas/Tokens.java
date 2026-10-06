import java.util.HashMap;
import java.util.Map;

/**
 * CLASE 1: TOKENS
 */
public class Tokens {

    
    // 1. PALABRAS RESERVADAS  -> códigos -1 a -20
    
    public static final Map<String, Integer> RESERVADAS = new HashMap<>();
    static {
        RESERVADAS.put("public",        -1);
        RESERVADAS.put("private",       -2);
        RESERVADAS.put("class",         -3);
        RESERVADAS.put("void",          -4);
        RESERVADAS.put("int",           -5);
        RESERVADAS.put("double",        -6);
        RESERVADAS.put("String",        -7);
        RESERVADAS.put("new",           -8);
        RESERVADAS.put("nextLine",      -9);
        RESERVADAS.put("nextInt",      -10);
        RESERVADAS.put("nextDouble",   -11);
        RESERVADAS.put("println",      -12);
        RESERVADAS.put("if",           -13);
        RESERVADAS.put("else",         -14);
        RESERVADAS.put("do",           -15);
        RESERVADAS.put("while",        -16);
        RESERVADAS.put("switch",       -17);
        RESERVADAS.put("case",         -18);
        RESERVADAS.put("default",      -19);
        RESERVADAS.put("return",       -20);
    }

    
    // 2. OPERADORES ARITMÉTICOS -> códigos -31 a -34

    public static final int SUMA          = -31; // +
    public static final int RESTA         = -32; // -
    public static final int MULTIPLICA    = -33; // *
    public static final int DIVIDE        = -34; // /
    public static final int PORCENTAJE    = -35; // %
    public static final int IGUAL         = -36; // =
    public static final int SUMA_SUMA     = -37; // ++
    public static final int RESTA_RESTA   = -38; // --
    public static final int SUMA_IGUAL    = -39; // +=
    public static final int RESTA_IGUAL   = -40; // -=



    // 3. OPERADORES RELACIONALES -> códigos -41 a -46
    
    public static final int MENOR         = -41; // <
    public static final int MENOR_IGUAL   = -42; // <=
    public static final int MAYOR         = -43; // >
    public static final int MAYOR_IGUAL   = -44; // >=
    public static final int IGUAL_IGUAL   = -45; // ==
    public static final int DIFERENTE     = -46; // !=


    
    // 4. OPERADORES LÓGICOS -> códigos -51 a -53
    
    public static final int NOT_LOGICO    = -51; // !
    public static final int AND_LOGICO    = -52; // &&
    public static final int OR_LOGICO     = -53; // || 

    
    // 5. IDENTIFICADORES -61 a -64 Y CONSTANTES -> códigos -71 a -73 
    
    public static final int IDENTIFICADOR = -61;


    public static final int CTE_ENTERA    = -71;
    public static final int CTE_REAL      = -72;
    public static final int CTE_STRING    = -73;

    
    // 6. CARACTERES ESPECIALES QUE SÍ GENERAN TOKEN -> -81 a -88
    
    public static final int PYCOMA            = -81; // ;
    public static final int CORCHETE_ABRE     = -82; // [
    public static final int CORCHETE_CIERRA   = -83; // ]
    public static final int COMA              = -84; // ,
    public static final int DOSPUNTOS         = -85; // :
    public static final int PARENTESIS_ABRE   = -86; // (
    public static final int PARENTESIS_CIERRA = -87; // )
    public static final int LLAVE_ABRE        = -88; // {
    public static final int LLAVE_CIERRA     = -89; // }



    
    // 7. CÓDIGO DE ERROR
    
    public static final int ERROR = -1000;

    /**
     * Regresa el código de una palabra reservada, o null si la cadena
     * no es una palabra reservada 
     */
    public static Integer codigoReservada(String palabra) {
        return RESERVADAS.get(palabra);
    }
/**No Generan token
 * 
*/
    public static final String[][] NO_GENERAN_TOKEN = {
            {"\"",   "Comilla: delimita las constantes String, no genera token propio"},
            {".",    "Punto: separador, no genera token propio"},
            {"BCO",  "Espacio en blanco"},
            {"TAB",  "Tabulador"},
            {"EOLN", "Fin de línea (salto de línea)"},
            {"EOF",  "Fin de archivo"}
    };

    /**
     * Imprimir el nombre de un token a partir
     * de su código. 
     */
    public static String nombre(int codigo) {
        for (Map.Entry<String, Integer> e : RESERVADAS.entrySet()) {
            if (e.getValue() == codigo) return e.getKey();
        }
        switch (codigo) {
            case SUMA:            return "+";
            case RESTA:           return "-";
            case MULTIPLICA:      return "*";
            case DIVIDE:          return "/";
            case PORCENTAJE:      return "%";
            case IGUAL:           return "=";
            case SUMA_SUMA:       return"++";
            case RESTA_RESTA:     return"--";
            case SUMA_IGUAL:      return"+=";
            case RESTA_IGUAL:     return"-=";
            case MENOR:           return "<";
            case MENOR_IGUAL:     return "<=";
            case MAYOR:           return ">";
            case MAYOR_IGUAL:     return ">=";
            case IGUAL_IGUAL:     return "==";
            case DIFERENTE:       return "!=";
            case NOT_LOGICO:      return "!";
            case AND_LOGICO:      return "&&";
            case OR_LOGICO:       return "||";
            case IDENTIFICADOR:   return "Identificador";
            case CTE_ENTERA:      return "Cte_entera";
            case CTE_REAL:        return "Cte_real";
            case CTE_STRING:      return "Cte_string";
            case PYCOMA:          return ";";
            case CORCHETE_ABRE:   return "[";
            case CORCHETE_CIERRA: return "]";
            case COMA:            return ",";
            case DOSPUNTOS:       return ":";
            case PARENTESIS_ABRE: return "(";
            case PARENTESIS_CIERRA: return ")";
            case LLAVE_ABRE: return "{";
            case LLAVE_CIERRA: return "}";
            case ERROR: return "ERROR";
            default: return "DESCONOCIDO(" + codigo + ")";
        }
    }
}