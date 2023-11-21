package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Store extends Instruction {
    private final Value val;
    private final Value addr;

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
        this.val = val;
        use(val);
        this.addr = addr;
        use(addr);
    }

    public String toString() {
        return "store i32 " + val.getReg() + ", i32* " + addr.getReg() + "\n";
    }

    public void toMips() {
        super.toMips();
        Register src = RegisterManager.k0;
        //先找出要赋值的寄存器来，并将其存放于k0中
        if (val instanceof ConstNumber n) {
            new Li(src, n.getVal());
        } else {
            src = MipsManager.instance().getTempVarByRegister(val, RegisterManager.k0);
        }

        if (addr instanceof LocalDecl) {
            new Sw(src, MipsManager.instance().getLocalVarAddr(addr), RegisterManager.sp);
        } else if (addr instanceof GlobalDecl) {
            new La(RegisterManager.k1, MipsManager.instance().getGlobalData(addr));
            new Sw(src, 0, RegisterManager.k1);
        } else {
            //算出来的地址
            MipsManager.instance().getTempVarByRegister(addr, RegisterManager.k1);
            //TODO:这里一定是k1吗??
            new Sw(src, 0, RegisterManager.k1);
        }
    }
}
