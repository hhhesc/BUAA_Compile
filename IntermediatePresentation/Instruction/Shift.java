package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.ALU.Sll;
import TargetCode.Instruction.ALU.Sra;
import TargetCode.Instruction.ALU.Srl;
import TargetCode.Instruction.Li;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

public class Shift extends Instruction {
    private final boolean shiftRight;
    private boolean logicalShiftRight = false;

    public Shift(boolean shiftRight, Value v, ConstNumber n) {
        super(IRManager.getInstance().declareTempVar(), ValueType.I32);
        this.shiftRight = shiftRight;
        use(v);
        use(n);
    }

    public String toString() {
        String instr = (shiftRight) ? "ashr" : "shl";
        return reg + " = " + instr + " i32 " + operandList.get(0).getReg() + ", " + operandList.get(1).getReg() + "\n";
    }

    public void toMips() {
        super.toMips();
        Register register = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = register == null;
        if (noRegAllocated) {
            register = RegisterManager.k0;
        }

        int shiftBit = ((ConstNumber) operandList.get(1)).getVal();
        if (operandList.get(0) instanceof ConstNumber n) {
            if (shiftRight) {
                if (logicalShiftRight) {
                    new Li(register, n.getVal() >> shiftBit);
                } else {
                    new Li(register, n.getVal() >>> shiftBit);
                }
            } else {
                new Li(register, n.getVal() << shiftBit);
            }
        } else {
            Register operand = MipsManager.instance().getTempVarByRegister(operandList.get(0), RegisterManager.k0);
            if (shiftRight) {
                if (logicalShiftRight) {
                    new Srl(register, operand, shiftBit);
                } else {
                    new Sra(register, operand, shiftBit);
                }
            } else {
                new Sll(register, operand, shiftBit);
            }
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, register);
        }
    }

    public ConstNumber toConstNumber() {
        assert operandList.get(0) instanceof ConstNumber;
        assert operandList.get(1) instanceof ConstNumber;
        int v1 = ((ConstNumber) operandList.get(0)).getVal();
        int v2 = ((ConstNumber) operandList.get(1)).getVal();
        int number;
        if (shiftRight) {
            number = (v1 << v2);
        } else {
            number = (v1 >> v2);
        }
        return new ConstNumber(number);
    }

    public void setLogicalShiftRight(boolean logicalRight) {
        this.logicalShiftRight = logicalRight;
    }

    public boolean isShiftRight(){
        return shiftRight;
    }

    public boolean isLogicalShiftRight(){
        return logicalShiftRight;
    }
}
