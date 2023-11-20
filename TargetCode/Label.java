package TargetCode;

public class Label extends MipsStmt {
    private final String name;

    public Label(String label) {
        name = label;
    }

    public String toString() {
        return name + ":\n";
    }

    public String getIdent() {
        return name;
    }
}
