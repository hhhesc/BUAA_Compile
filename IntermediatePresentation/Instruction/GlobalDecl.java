package IntermediatePresentation.Instruction;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.GlobalData.Word;

import java.util.ArrayList;

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

    public void toMips() {
        Value init = operandList.get(0);
        if (init instanceof ArrayInitializer aInit) {
            ArrayList<Integer> vals = new ArrayList<>();
            if (aInit.getVals().size() == 0) {
                for (int i = 0; i < aInit.getLength(); i++) {
                    vals.add(0);
                }
            } else {
                for (Value v : aInit.getVals()) {
                    vals.add(0, ((ConstNumber) v).getVal());
                }
            }
            new Word(this, getName(), vals);
        } else {
            new Word(this, getName(), ((ConstNumber) init).getVal());
        }
    }
}
