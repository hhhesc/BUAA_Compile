package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Param;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import Optimizer.Optimizer;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Memory.OffsetRegAddr;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Store extends Instruction {
    private Value addr;

    public Store(Value val, Value addr) {
        super("STORE");
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
        use(val);
        this.addr = addr;
        use(addr);
    }

    public Value getSrc() {
        return operandList.get(0);
    }

    public Value getAddr() {
        if (operandList.size() <= 1) {
            return addr;
        } else {
            return operandList.get(1);
        }
    }

    public String toString() {
        return "store i32 " + operandList.get(0).getReg() + ", i32* " + operandList.get(1).getReg() + "\n";
    }

    public void toMips() {
        super.toMips();
        Value addr = operandList.get(1);
        Register src = RegisterManager.k0;
        //先找出要赋值的寄存器来，并将其存放于k0中
        Value val = operandList.get(0);
        if (val instanceof ConstNumber n) {
            new Li(src, n.getVal());
        } else {
            src = MipsManager.instance().getTempVarByRegister(val, RegisterManager.k0);
        }

        if (addr instanceof LocalDecl) {
            new Sw(src, MipsManager.instance().getLocalVarAddr(addr), RegisterManager.sp);
        } else if (addr instanceof GlobalDecl) {
            new Sw(src, MipsManager.instance().getGlobalData(addr), RegisterManager.zero);
        } else {
            //算出来的地址
            new Sw(src, 0, MipsManager.instance().getTempVarByRegister(addr, RegisterManager.k1));
        }
    }

    public boolean isUseless() {
        return false;
    }

    public boolean isDefInstr() {
        return false;
    }

    public void setAddr(Value addr) {
        this.addr = addr;
    }

    public boolean hasSideEffect() {
        Value addr = getAddr();
        while (addr instanceof GetElementPtr gep) {
            for (User user : gep.getUserList()) {
                if (user instanceof Call call && Optimizer.instance().hasSideEffect(call.getCallingFunction())) {
                    //如果被当作了参数，那一定是有副作用了
                    return true;
                }
            }
            addr = gep.getPtr();
        }

        for (User user : addr.getUserList()) {
            if (user instanceof Call call && Optimizer.instance().hasSideEffect(call.getCallingFunction())) {
                return true;
            }
        }

        //如果是全局的，或者是从参数里传过来，那就一定有副作用
        return addr instanceof GlobalDecl || getBlock().getFunction().getParam().getParams().contains(addr);
    }
}
