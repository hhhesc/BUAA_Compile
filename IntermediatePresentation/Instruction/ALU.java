package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.ALU.Add;
import TargetCode.Instruction.ALU.Addi;
import TargetCode.Instruction.ALU.Div;
import TargetCode.Instruction.ALU.Mfhi;
import TargetCode.Instruction.ALU.Mflo;
import TargetCode.Instruction.ALU.Mult;
import TargetCode.Instruction.ALU.Neg;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.ALU.Sub;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class ALU extends Instruction {
    private final String instr;

    private boolean mulFromHi = false;

    public ALU(Value operand1, String operator, Value operand2) {
        super(IRManager.getInstance().declareTempVar(), ValueType.I32);
        operand1 = turntoI32WhileBuilding(operand1);
        use(operand1);
        operand2 = turntoI32WhileBuilding(operand2);
        use(operand2);
        switch (operator) {
            case "+" -> instr = "add";
            case "-" -> instr = "sub";
            case "*" -> instr = "mul";
            case "/" -> instr = "sdiv";
            case "%" -> instr = "srem";
            default -> instr = "UNKOWN OP";
        }
    }

    public boolean isConst() {
        boolean flag = true;
        if (instr.equals("sdiv") || instr.equals("srem")) {
            flag = operandList.get(1) instanceof ConstNumber n && n.getVal() != 0;
        }
        return !mulFromHi && operandList.get(0) instanceof ConstNumber &&
                operandList.get(1) instanceof ConstNumber && flag;
    }

    public void setMulFromHi(boolean mulFromHi) {
        this.mulFromHi = mulFromHi;
    }

    public ConstNumber toConstNumber() {
        assert operandList.get(0) instanceof ConstNumber;
        assert operandList.get(1) instanceof ConstNumber;
        int v1 = ((ConstNumber) operandList.get(0)).getVal();
        int v2 = ((ConstNumber) operandList.get(1)).getVal();
        if (v2 == 0 && (instr.equals("sdiv") || instr.equals("srem"))) {
            return new ConstNumber(0);
        }
        int number = switch (instr) {
            case "add" -> v1 + v2;
            case "sub" -> v1 - v2;
            case "mul" -> v1 * v2;
            case "sdiv" -> v1 / v2;
            case "srem" -> v1 % v2;
            default -> 0;
        };

        return new ConstNumber(number);
    }

    public String toString() {
        return reg + " = " + instr + " i32 " + operandList.get(0).getReg()
                + ", " + operandList.get(1).getReg() + "\n";
    }

    public void toMips() {
        super.toMips();


        Register dest = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = dest == null;
        if (noRegAllocated) {
            dest = RegisterManager.k0;
        }

        Value oprand1 = operandList.get(0);
        Value oprand2 = operandList.get(1);

        if (oprand1 instanceof ConstNumber && oprand2 instanceof ConstNumber) {
            new Li(dest, toConstNumber().getVal());
            if (noRegAllocated) {
                MipsManager.instance().pushTempVar(this, dest);
            }
            return;
        }

        switch (instr) {
            case "add" -> {
                if (oprand1 instanceof ConstNumber) {
                    Register src = MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k0);
                    new Addi(dest, src, ((ConstNumber) oprand1).getVal());
                } else if (oprand2 instanceof ConstNumber) {
                    Register src = MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0);
                    new Addi(dest, src, ((ConstNumber) oprand2).getVal());
                } else {
                    new Add(dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                            MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1));
                }
            }
            case "sub" -> {
                if (oprand1 instanceof ConstNumber n) {
                    //这里是 constnumber - register，不能一步完成
                    Register src = MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1);
                    if (n.getVal() == 0) {
                        new Neg(dest, src);
                    } else {
                        new Li(RegisterManager.k0, n.getVal());
                        new Sub(dest, RegisterManager.k0, src);
                    }
                } else if (oprand2 instanceof ConstNumber) {
                    Register src = MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0);
                    new Addi(dest, src, -((ConstNumber) oprand2).getVal());
                } else {
                    new Sub(dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                            MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1));
                }
            }

            default -> {

                Register register0 = RegisterManager.k0;
                Register register1 = RegisterManager.k1;

                if (oprand1 instanceof ConstNumber n) {
                    new Li(register0, n.getVal());
                } else {
                    register0 = MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0);
                }

                if (oprand2 instanceof ConstNumber n) {
                    new Li(register1, n.getVal());
                } else {
                    register1 = MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1);
                }

                switch (instr) {
                    case "mul" -> {
                        new Mult(register0, register1);
                        if (mulFromHi) {
                            new Mfhi(dest);
                        } else {
                            new Mflo(dest);
                        }
                    }

                    case "sdiv" -> {
                        new Div(register0, register1);
                        new Mflo(dest);
                    }

                    case "srem" -> {
                        new Div(register0, register1);
                        new Mfhi(dest);
                    }
                    default -> {
                    }
                }
            }
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, dest);
        }
    }

    public ArrayList<String> GVNHash() {
        //ALU里+/*是符合交换律的，所以单独写一下
        if (instr.equals("add") || instr.equals("mul")) {
            ArrayList<String> ret = new ArrayList<>();
            ret.add(instr + " i32 " + operandList.get(0).getReg()
                    + ", " + operandList.get(1).getReg() + "\n");
            ret.add(instr + " i32 " + operandList.get(1).getReg()
                    + ", " + operandList.get(0).getReg() + "\n");
            return ret;
        } else {
            return super.GVNHash();
        }
    }

    public String getAluType() {
        return instr;
    }

    public Value getOperand1() {
        return operandList.get(0);
    }

    public Value getOperand2() {
        return operandList.get(1);
    }

    public String getOperator() {
        return switch (instr) {
            case "add" -> "+";
            case "sub" -> "-";
            case "mul" -> "*";
            case "sdiv" -> "/";
            case "srem" -> "%";
            default -> "UNKOWN OP";
        };
    }
}
