package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.MipsManager;

import java.util.ArrayList;

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

    public void toMips() {
        MipsManager.instance().allocInStackBy(this, vType.getLength());
    }

    public ArrayList<String> GVNHash() {
        return null;
    }

    public int getLen(){
        return vType.getLength();
    }
}
