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

import java.util.ArrayList;

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
        String op = switch (cond) {
            case "eq" -> "seq";
            case "ne" -> "sne";
            default -> cond;
        };

        Value oprand1 = operandList.get(0);
        Value oprand2 = operandList.get(1);

        Register dest = RegisterManager.instance().getRegOf(this);
        boolean noRegAllocated = dest == null;
        if (noRegAllocated) {
            dest = RegisterManager.k0;
        }


        if (isConst()) {
            new Li(dest, toConstNumber().getVal());
        } else if (oprand1 instanceof ConstNumber n) {
            op = switch (op) {
                case "sle" -> "sge";
                case "sge" -> "sle";
                case "sgt" -> "slt";
                case "slt" -> "sgt";
                default -> op;
            };
            if (op.equals("slt") && (n.getVal() > 32767 || n.getVal() < -32768)) {
                new Li(RegisterManager.k1, n.getVal());
                new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k0),
                        RegisterManager.k1);
            } else {
                new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k0),
                        n.getVal());
            }
        } else if (oprand2 instanceof ConstNumber n) {
            if (op.equals("slt") && (n.getVal() > 32767 || n.getVal() < -32768)) {
                new Li(RegisterManager.k1, n.getVal());
                new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                        RegisterManager.k1);
            } else {
                new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                        n.getVal());
            }
        } else {
            new Scmp(op, dest, MipsManager.instance().getTempVarByRegister(oprand1, RegisterManager.k0),
                    MipsManager.instance().getTempVarByRegister(oprand2, RegisterManager.k1));
        }

        if (noRegAllocated) {
            MipsManager.instance().pushTempVar(this, dest);
        }
    }

    public ArrayList<String> GVNHash() {
        ArrayList<String> ret = new ArrayList<>();
        switch (cond) {
            case "slt":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "sgt" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            case "sle":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "sge" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            case "sgt":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "slt" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            case "sge":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "sle" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            case "eq":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "eq" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            case "ne":
                ret.add("icmp " + cond + " i32 " + operandList.get(0).getReg() +
                        ", " + operandList.get(1).getReg() + "\n");
                ret.add("icmp " + "ne" + " i32 " + operandList.get(1).getReg() +
                        ", " + operandList.get(0).getReg() + "\n");
                break;
            default:
                return super.GVNHash();
        }
        return ret;
    }

    public ConstNumber toConstNumber() {
        assert operandList.get(0) instanceof ConstNumber;
        assert operandList.get(1) instanceof ConstNumber;
        int val1 = ((ConstNumber) operandList.get(0)).getVal();
        int val2 = ((ConstNumber) operandList.get(1)).getVal();
        boolean res = switch (cond) {
            case "sgt" -> val1 > val2;
            case "sle" -> val1 <= val2;
            case "sge" -> val1 >= val2;
            case "eq" -> val1 == val2;
            case "ne" -> val1 != val2;
            case "slt" -> val1 < val2;
            default -> false;
        };
        if (res) {
            return new ConstNumber(1);
        } else {
            return new ConstNumber(0);
        }
    }

    public boolean isConst() {
        return operandList.get(0) instanceof ConstNumber && operandList.get(1) instanceof ConstNumber;
    }

    public String getCond() {
        return cond;
    }

    public String getOperator(){
        return switch (cond) {
            case "sgt" -> ">";
            case "sle" -> "<=";
            case "sge" -> ">=";
            case "eq" -> "==";
            case "ne" -> "!=";
            case "slt" -> "<";
            default -> "";
        };
    }
}
