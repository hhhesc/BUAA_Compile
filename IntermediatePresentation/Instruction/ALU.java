package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class ALU extends Instruction {
    String instr;

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

    public ConstNumber toConstNumber() {
        assert operandList.get(0) instanceof ConstNumber;
        assert operandList.get(1) instanceof ConstNumber;
        int v1 = ((ConstNumber) operandList.get(0)).getVal();
        int v2 = ((ConstNumber) operandList.get(1)).getVal();
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
}
