package LexicalAnalyse.Words;

import java.util.regex.Pattern;

public class IntConst extends Word {
    public static Pattern pattern;

    static {
        reg = "(\\d+)";
        pattern = Pattern.compile(reg);
    }

    public IntConst(String srcStr, int lineNumber) {
        super(srcStr, lineNumber);
    }

    public String getTokenType() {
        return "INTCON";
    }

    public int getValue() {
        if (srcStr.startsWith("0x")) {
            return Integer.parseInt(srcStr.substring(2), 16);
        } else if (srcStr.startsWith("0") && !srcStr.equals("0")) {
            return Integer.parseInt(srcStr.substring(1), 8);
        } else {
            return Integer.parseInt(srcStr);
        }
    }
}
