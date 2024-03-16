package TargetCode.Instruction.Jump;

import TargetCode.Instruction.MipsInstr;
import TargetCode.Label;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Branch extends MipsInstr {
    private final Label dest;
    private final Object operand1;
    private final Object operand2;

    private final String op;

    public Branch(Label dest, Object operand1, Object operand2, String op) {
        super();
        this.dest = dest;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.op = op;
    }

    public String toString() {
        return "b" + op + " " + operand1 + ", " + operand2 + ", " + dest.getIdent() + "\n";
    }

    public Label getLabel() {
        return dest;
    }

    public ArrayList<Register> operandRegs() {
        ArrayList<Register> ret = new ArrayList<>();
        if (operand1 instanceof Register register) {
            ret.add(register);
        }
        if (operand2 instanceof Register register) {
            ret.add(register);
        }
        return ret;
    }
}
