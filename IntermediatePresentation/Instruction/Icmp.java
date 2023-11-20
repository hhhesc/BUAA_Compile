package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import TargetCode.Instruction.ALU.Scmp;
import TargetCode.Instruction.Li;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import javax.xml.stream.FactoryConfigurationError;

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

    public void toMips() {
        super.toMips();
        String op = cond;
        if (cond.equals("eq")) {
            op = "seq";
        } else if (cond.equals("ne")) {
            op = "sne";
        }

        if (op.equals("slt")) {
            //slt不支持立即数，变为sgt
            Value temp = operandList.get(0);
            operandList.set(0, operandList.get(1));
            operandList.set(1, temp);
            op = "sgt";
        }

        Register dest = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = dest == null;
        if (noRegAllocated) {
            dest = RegisterManager.k0;
        }

        Value oprand1 = operandList.get(0);
        Value oprand2 = operandList.get(1);
        if (oprand1 instanceof ConstNumber n1 && oprand2 instanceof ConstNumber n2) {
            //这里也不应该是等号吧
            int val1 = n1.getVal();
            int val2 = n2.getVal();
            boolean res = switch (op) {
                case "sgt" -> val1 > val2;
                case "sle" -> val1 <= val2;
                case "sge" -> val1 >= val2;
                case "seq" -> val1 == val2;
                case "sne" -> val1 != val2;
                default -> false;
            };
            if (res) {
                new Li(dest, 1);
            } else {
                new Li(dest, 0);
            }
        } else if (oprand1 instanceof ConstNumber n) {
            //这里顺序不能交换的
            new Li(RegisterManager.k1, n.getVal());
            new Scmp(op, dest, RegisterManager.k1,
                    MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k0));
        } else if (oprand2 instanceof ConstNumber) {
            new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                    ((ConstNumber) oprand2).getVal());
        } else {
            new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                    MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1));
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, dest);
        }
    }
}
