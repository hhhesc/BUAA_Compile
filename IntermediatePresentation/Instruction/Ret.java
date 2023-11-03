package IntermediatePresentation.Instruction;

import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class Ret extends Instruction {
    private Value retValue;

    public Ret() {
        super("RET", ValueType.NULL);
    }

    public Ret(Value retValue) {
        super("RET", ValueType.I32);
        this.retValue = retValue;
    }

    public String toString() {
        if (vType == ValueType.NULL) {
            return "ret void\n";
        } else {
            return "ret " + getTypeString() + " " + retValue.getReg() + "\n";
        }
    }
}
