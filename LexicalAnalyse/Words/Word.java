package LexicalAnalyse.Words;

import java.util.HashSet;

public class Word {
    protected String srcStr;
    protected int lineNumber;
    protected static String reg;
    protected static HashSet<String> patterns = new HashSet<>();
    //TODO:加个startIndex和endIndex

    public Word(String srcStr, int lineNumber) {
        this.srcStr = srcStr;
        this.lineNumber = lineNumber;
    }

    public String getSrcStr() {
        return this.srcStr;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Word w)) {
            return false;
        } else {
            return w.getSrcStr().equals(srcStr) && w.getLineNumber() == lineNumber;
        }
    }

    public String getTokenType() {
        return srcStr;
    }

    public int getLineNumber() {
        return lineNumber;
    }

}
