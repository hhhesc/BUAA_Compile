package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.MipsManager;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Instruction extends User {
    private BasicBlock block;

    public Instruction(String regName, ValueType VType) {
        super(regName, VType);
        IRManager.getInstance().instrCreated(this);
        block = IRManager.getInstance().getCurBlock();
    }

    public Instruction(String reg) {
        super(reg, ValueType.NULL);
        IRManager.getInstance().instrCreated(this);
        block = IRManager.getInstance().getCurBlock();
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

    public BasicBlock getBlock() {
        return block;
    }

    public void setBlock(BasicBlock block) {
        this.block = block;
    }

    public void destroy() {
        ArrayList<User> users = new ArrayList<>(userList);
        for (User user : users) {
            user.removeOperand(this);
        }
        for (Value operand : operandList) {
            operand.removeUser(this);
        }
    }

    public boolean isDefInstr() {
        return true;
    }

    public ArrayList<String> GVNHash() {
        String str = toString();
        if (str.contains("=")) {
            ArrayList<String> ret = new ArrayList<>();
            ret.add(str.substring(str.indexOf("=") + 1));
            return ret;
        } else {
            return null;
        }
    }

}
