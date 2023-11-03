package IntermediatePresentation.Instruction;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class GlobalDecl extends Instruction {

    public GlobalDecl(Value val) {
        super(IRManager.getInstance().declareVar(), ValueType.PI32);
        use(val);
        IRManager.getModule().addGobalDecl(this);
        if (val instanceof ArrayInitializer aInit) {
            vType = new ValueType(aInit.getLength());
        }
    }

    public String toString() {
        Value init = operandList.get(0);
        if (init instanceof ArrayInitializer aInit) {
            return reg + " = dso_local global " + vType + " " + aInit + "\n ";
        } else {
            return reg + " = dso_local global i32 " + init + "\n";
        }
    }
}
