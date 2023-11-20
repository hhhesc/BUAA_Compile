package IntermediatePresentation.Instruction;

import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Move;
import TargetCode.Instruction.Syscall;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Call extends Instruction {
    private final ArrayList<Value> params = new ArrayList<>();
    private final Function function;

    public Call(Function function, ArrayList<Value> params) {
        super("CALL", function.getType());
        this.function = function;
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
            this.params.add(v);
        }
    }

    public Call(Function function, Value... params) {
        super("CALL");
        this.function = function;
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
            this.params.add(v);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (function.isVoid()) {
            sb.append("call void ");
        } else {
            sb.append(reg).append(" = call ").append(function.getTypeString()).append(" ");
        }
        sb.append(function.getReg()).append("(");

        for (Value param : params) {
            sb.append(param.getTypeString()).append(" ");
            sb.append(param.getReg()).append(", ");
        }
        if (params.size() != 0) {
            sb = new StringBuilder(sb.substring(0, sb.length() - 2));
        }
        sb.append(")\n");
        return sb.toString();
    }

    public void toMips() {
        super.toMips();
        if (function.equals(Function.putint)) {
            MipsManager.instance().push(RegisterManager.a0);
            Value param = params.get(0);
            if (param instanceof ConstNumber n) {
                new Li(RegisterManager.a0, n.getVal());
            } else {
                new Move(RegisterManager.a0,
                        MipsManager.instance().getTempVarByRegister(param, RegisterManager.k0));
            }
            new Li(RegisterManager.v0, 1);
            new Syscall();
            MipsManager.instance().popTo(RegisterManager.a0);
        } else if (function.equals(Function.getint)) {
            new Li(RegisterManager.v0, 5);
            Register dest = RegisterManager.instance().getRegOf(this);
            boolean noRegAllocated = dest == null;
            if (dest == null) {
                dest = RegisterManager.t0;
            }
            new Syscall();
            new Move(dest, RegisterManager.v0);
            if (noRegAllocated) {
                MipsManager.instance().pushTempVar(this, dest);
            }
        } else {
            /*
                1. 保存现场
                   保存$fp,$ra以及其他需要使用的寄存器（目前仅有$a0-$a3)
                   TODO:目前不需要考虑寄存器的保存策略，之后可能要做
             */

            //保存当前参数
            for (int i = 0; i < 4; i++) {
                MipsManager.instance().push(RegisterManager.instance().getParamRegister(i));
            }

            //保存$ra和$fp
            MipsManager.instance().push(RegisterManager.ra);
            MipsManager.instance().push(RegisterManager.fp);

            /*
                2. 参数传递
                   将目标函数的第五个及之后的参数压入$sp栈中，然后移动$sp
             */
            //保存前四个参数
            for (int i = 0; i < 4; i++) {
                if (i == params.size()) {
                    break;
                } else {
                    if (params.get(i) instanceof ConstNumber n) {
                        new Li(RegisterManager.instance().getParamRegister(i), n.getVal());
                    } else {
                        Register register =
                                MipsManager.instance().getTempVarByRegister(params.get(i), RegisterManager.k0);
                        new Move(RegisterManager.instance().getParamRegister(i), register);
                    }
                    MipsManager.instance().allocEmptyStackSpace();
                }
            }

            if (params.size() >= 5) {
                for (int i = 4; i < params.size(); i++) {
                    Register register = RegisterManager.k0;
                    if (params.get(i) instanceof ConstNumber n) {
                        new Li(register, n.getVal());
                    } else {
                        register = MipsManager.instance().getTempVarByRegister(params.get(i), RegisterManager.k0);
                    }
                    MipsManager.instance().push(register);
                }
            }

            //移动$sp
            MipsManager.instance().saveSp(params.size());

            //3. 跳转
            new Jal(MipsManager.instance().getFunctionLabel(function));

            /*
                4. 返回，恢复现场
                    1) 保存返回值
                    2) 恢复当前函数参数
                    2) 恢复$ra和$fp
                    3) 移动$sp
             */

            //移动$sp
            MipsManager.instance().resetSp();
            //恢复寄存器$ra,$fp
            MipsManager.instance().popTo(RegisterManager.fp);
            MipsManager.instance().popTo(RegisterManager.ra);

            for (int i = 3; i >= 0; i--) {
                MipsManager.instance().popTo(RegisterManager.instance().getParamRegister(i));
            }

            //保存返回值
            if (!function.isVoid()) {
                Register dest = RegisterManager.instance().getRegOf(this);
                if (dest == null) {
                    MipsManager.instance().pushTempVar(this, RegisterManager.v0);
                } else {
                    new Move(dest, RegisterManager.v0);
                }
            }
        }
    }
}
