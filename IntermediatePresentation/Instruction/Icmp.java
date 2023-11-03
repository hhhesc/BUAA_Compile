package IntermediatePresentation.Instruction;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

public class Icmp extends Instruction {
    private final String cond;

    public Icmp(String op, Value oprand1, Value oprand2) {
        super(IRManager.getInstance().declareTempVar(), ValueType.I1);
        oprand1 = turntoI32WhileBuilding(oprand1);
        use(oprand1);
        oprand2 = turntoI32WhileBuilding(oprand2);
        use(oprand2);
        cond = switch (op) {
            case "<" -> "slt";
            case "<=" -> "sle";
            case ">" -> "sgt";
            case ">=" -> "sge";
            case "==" -> "eq";
            case "!=" -> "ne";
            default -> "UNKOWNCMPOP";
        };
    }

    public String toString() {
        return reg + " = icmp " + cond + " i32 " + operandList.get(0).getReg() +
                ", " + operandList.get(1).getReg() + "\n";
    }
}
