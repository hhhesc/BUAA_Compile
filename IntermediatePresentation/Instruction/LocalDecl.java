package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.ValueType;

public class LocalDecl extends Instruction {
    public LocalDecl() {
        super(IRManager.getInstance().declareVar(), ValueType.PI32);
    }

    public LocalDecl(int len) {
        super(IRManager.getInstance().declareVar(), ValueType.ARRAY);
        vType = new ValueType(len);
    }

    public String toString() {
        return reg + " = alloca " + vType.getRefTypeString() + "\n";
    }
}
