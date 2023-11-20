package TargetCode;

public class Annotation extends MipsStmt {
    private final String content;

    public Annotation(String content) {
        this.content = content;
    }

    public String toString() {
        return "\n# " + content;
    }
}
