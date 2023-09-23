package LexicalAnalyse.Words;

import java.util.regex.Pattern;

public class FormatString extends Word {
    public static Pattern pattern;

    public FormatString(String srcStr, int lineNumber) {
        super(srcStr, lineNumber);
    }

    static {
        reg = "\"((%d)|(.*))?\"";
        pattern = Pattern.compile(reg);
    }

    public String getTokenType() {
        return "STRCON";
    }


}
