package LexicalAnalyse.Words;

import java.util.regex.Pattern;

public class Ident extends Word {
    public static Pattern pattern;

    static {
        reg = "[a-zA-Z_]\\w*";
        pattern = Pattern.compile(reg);
    }

    public Ident(String srcStr, int lineNumber) {
        super(srcStr, lineNumber);
    }

    public String getTokenType() {
        return "IDENFR";
    }
}
