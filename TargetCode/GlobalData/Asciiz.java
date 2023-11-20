package TargetCode.GlobalData;

import IntermediatePresentation.Value;

public class Asciiz extends DataDecl {
    private final String str;

    public Asciiz(Value v, String name, String str) {
        super(v, name);
        this.str = str;
    }

    public String toString() {
        return spaceName + ": .asciiz \"" + str + "\"\n";
    }

}
