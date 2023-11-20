package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.MipsManager;

public class Instruction extends User {
    public Instruction(String regName, ValueType VType) {
        super(regName, VType);
        IRManager.getInstance().instrCreated(this);
    }

    public Instruction(String reg) {
        super(reg, ValueType.NULL);
        IRManager.getInstance().instrCreated(this);
    }

    public String toString() {
        return reg + "\n";
    }

    public Value turntoI32WhileBuilding(Value v) {
        //丑陋的架构补丁
        Value val = v;
        if (val.isPointer()) {
            IRManager.getInstance().deleteInstruction();
            val = new Load(IRManager.getInstance().declareTempVar(), val);
            IRManager.getInstance().instrCreated(this);
        }
        if (val.getType() != ValueType.I32) {
            IRManager.getInstance().deleteInstruction();
            val = new ZextTo(val, ValueType.I32);
            IRManager.getInstance().instrCreated(this);
        }
        return val;
    }

    public void toMips() {
        MipsManager.getFile().insertAnnotation(this.toString());
    }
}
