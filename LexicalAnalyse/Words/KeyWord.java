package LexicalAnalyse.Words;

import java.util.HashMap;
import java.util.regex.Pattern;

public class KeyWord extends Word {
    public static Pattern pattern;
    private static final HashMap<String, String> tokenMap = new HashMap<>();

    static {
        tokenMap.put("if", "IFTK");
        tokenMap.put("else", "ELSETK");
        tokenMap.put("for","FORTK");
        tokenMap.put("getint","GETINTTK");
        tokenMap.put("printf","PRINTFTK");
        tokenMap.put("continue", "CONTINUETK");
        tokenMap.put("break", "BREAKTK");
        tokenMap.put("return", "RETURNTK");
        tokenMap.put("int", "INTTK");
        tokenMap.put("void","VOIDTK");
        tokenMap.put("const","CONSTTK");
        tokenMap.put("main","MAINTK");
        StringBuilder builder = new StringBuilder();
        for (String s : tokenMap.keySet()) {
            builder.append(s).append("|");
        }
        reg = builder.substring(0, builder.length() - 1);
        pattern = Pattern.compile(reg);
    }

    public KeyWord(String srcStr, int lineNumber) {
        super(srcStr, lineNumber);
    }

    public String getTokenType() {
        return tokenMap.getOrDefault(srcStr, null);
    }
}
