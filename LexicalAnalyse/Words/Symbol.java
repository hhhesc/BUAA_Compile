package LexicalAnalyse.Words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Symbol extends Word {
    public static Pattern pattern;
    private static final HashMap<String, String> tokenMap = new HashMap<>();

    static {
        tokenMap.put("==", "EQL");
        tokenMap.put("!=", "NEQ");
        tokenMap.put("<=", "LEQ");
        tokenMap.put(">=", "GEQ");
        tokenMap.put("!", "NOT");
        tokenMap.put("&&", "AND");
        tokenMap.put("||", "OR");
        tokenMap.put("=", "ASSIGN");
        tokenMap.put(",", "COMMA");
        tokenMap.put(";", "SEMICN");
        tokenMap.put("(", "LPARENT");
        tokenMap.put(")", "RPARENT");
        tokenMap.put("[", "LBRACK");
        tokenMap.put("]", "RBRACK");
        tokenMap.put("{", "LBRACE");
        tokenMap.put("}", "RBRACE");
        tokenMap.put("+", "PLUS");
        tokenMap.put("-", "MINU");
        tokenMap.put("*", "MULT");
        tokenMap.put("/", "DIV");
        tokenMap.put("%", "MOD");
        tokenMap.put("<", "LSS");
        tokenMap.put(">", "GRE");
        patterns = new HashSet<>(tokenMap.keySet());
        patterns.remove("(");
        patterns.add("\\(");
        patterns.remove(")");
        patterns.add("\\)");
        patterns.remove("[");
        patterns.add("\\[");
        patterns.remove("]");
        patterns.add("\\]");
        patterns.remove("{");
        patterns.add("\\{");
        patterns.remove("}");
        patterns.add("\\}");
        patterns.remove("+");
        patterns.add("\\+");
        patterns.remove("*");
        patterns.add("\\*");
        patterns.remove("||");
        patterns.add("\\|\\|");

        StringBuilder builder = new StringBuilder();
        for (String s : patterns) {
            if (s.length() > 1) {
                builder.append(s).append("|");
            }
        }
        for (String s : patterns) {
            if (s.length() == 1) {
                builder.append(s).append("|");
            }
        }
        reg = builder.substring(0, builder.length() - 1);
        pattern = Pattern.compile(reg);
    }

    public Symbol(String srcStr, int lineNumber) {
        super(srcStr, lineNumber);
    }

    public String getTokenType() {
        return tokenMap.getOrDefault(srcStr, null);
    }
}
