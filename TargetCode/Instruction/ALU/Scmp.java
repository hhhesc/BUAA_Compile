package TargetCode.Instruction.ALU;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Register;

import java.util.ArrayList;
import java.util.Objects;

public class Scmp extends MipsInstr {
    private String op;
    private final Register dest;
    private final Register operand1Reg;
    private Register operand2Reg = null;
    private int operand2Num;

    public Scmp(String op, Register dest, Register operand1Reg, Register oprand2) {
        super();
        this.op = op;
        this.dest = dest;
        this.operand1Reg = operand1Reg;
        this.operand2Reg = oprand2;
    }

    public Scmp(String op, Register dest, Register operand1Reg, int oprand2) {
        this.op = op;
        this.dest = dest;
        this.operand1Reg = operand1Reg;
        this.operand2Num = oprand2;
    }

    public String toString() {
        if (operand2Reg == null && op.equals("slt")) {
            op = "slti";
        }
        return op + " " + dest + ", " + operand1Reg + ", " +
                Objects.requireNonNullElseGet(operand2Reg, () -> operand2Num) + "\n";
    }

    public Register putToRegister() {
        return dest;
    }

    public Register getOperand1() {
        return operand1Reg;
    }

    public Object getOperand2() {
        return Objects.requireNonNullElseGet(operand2Reg, () -> operand2Num);
    }

    public ArrayList<Register> operandRegs() {
        ArrayList<Register> ret = new ArrayList<>();
        ret.add(operand1Reg);
        if (operand2Reg != null) {
            ret.add(operand2Reg);
        }
        return ret;
    }
}
