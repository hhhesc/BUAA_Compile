package TargetCode.GlobalData;

import IntermediatePresentation.Value;

import java.util.ArrayList;
import java.util.Collections;

public class Word extends DataDecl {
    private final ArrayList<Integer> init;

    public Word(Value v, String name, int init) {
        super(v, name);
        this.init = new ArrayList<>();
        this.init.add(init);
    }

    public Word(Value v, String name, ArrayList<Integer> init) {
        super(v, name);
        Collections.reverse(init);
        this.init = init;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(spaceName).append(": .word ");
        for (int initV : init) {
            sb.append(initV).append(",");
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("\n");
        return sb.toString();
    }
}
