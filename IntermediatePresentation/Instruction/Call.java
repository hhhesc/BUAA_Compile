package IntermediatePresentation.Instruction;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Value;
import Optimizer.Optimizer;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.Instruction.Move;
import TargetCode.Instruction.Syscall;
import TargetCode.MipsManager;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;

public class Call extends Instruction {

    public Call(Function function, ArrayList<Value> params) {
        super("CALL", function.getType());
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
        }
    }

    public Call(Function function, Value... params) {
        super("CALL");
        if (function.isVoid()) {
            use(function);
        } else {
            reg = IRManager.getInstance().declareTempVar();
            use(function);
        }

        for (Value v : params) {
            use(v);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Function function = (Function) operandList.get(0);
        if (function.isVoid()) {
            sb.append("call void ");
        } else {
            sb.append(reg).append(" = call ").append(function.getTypeString()).append(" ");
        }
        sb.append(function.getReg()).append("(");


        for (Value param : operandList) {
            if (!(param instanceof Function)) {
                sb.append(param.getTypeString()).append(" ");
                sb.append(param.getReg()).append(", ");
            }
        }
        if (operandList.size() != 1) {
            sb = new StringBuilder(sb.substring(0, sb.length() - 2));
        }
        sb.append(")\n");
        return sb.toString();
    }

    public void toMips() {
        super.toMips();
        Function function = (Function) operandList.get(0);
        int curParamN = getBlock().getFunction().getParam().getParams().size();
        if (function.equals(Function.putint)) {
            Value param = operandList.get(1);
            if (param instanceof ConstNumber n) {
                new Li(RegisterManager.a0, n.getVal());
            } else {
                new Move(RegisterManager.a0,
                        MipsManager.instance().getTempVarByRegister(param, RegisterManager.k0));
            }
            new Li(RegisterManager.v0, 1);
            new Syscall();
        } else if (function.equals(Function.getint)) {
            new Li(RegisterManager.v0, 5);
            Register dest = RegisterManager.instance().getRegOf(this);
            boolean noRegAllocated = dest == null;
            if (noRegAllocated) {
                dest = RegisterManager.k0;
            }
            new Syscall();

            if (userList.size() != 0) {
                new Move(dest, RegisterManager.v0);
                if (noRegAllocated) {
                    MipsManager.instance().pushTempVar(this, dest);
                }
            }
        } else {
            ArrayList<Value> params = new ArrayList<>(operandList);
            params.remove(0);
            /*
                1. 保存现场
                   保存$ra和已分配的$st寄存器，$a不用保存，它们已经在栈上有空间了
             */
            //保存$ra
            MipsManager.instance().push(RegisterManager.ra);
            ArrayList<Register> allocatedRegisters = new ArrayList<>();
            if (Optimizer.instance().hasDispatched()) {
                //已经分配，并将要使用的寄存器;也即在本指令之前的那个节点处，所有活跃变量分配到的寄存器
                allocatedRegisters = RegisterManager.instance().activeRegistersWhenCall(this);
                //需要保存的寄存器是这次call所可能调用的所有函数使用寄存器的闭包
                ArrayList<Register> registersMayUse =
                        new ArrayList<>(RegisterManager.instance().shouldSaveRegsWhenCall(getCallingFunction()));
                allocatedRegisters.retainAll(registersMayUse);
                for (Register register : allocatedRegisters) {
                    MipsManager.instance().push(register);
                }
            }


            /*
                2. 参数传递
                   将目标函数的第四个及之后的参数压入$sp栈中，然后移动$sp
                   建立valueToOffset的目的是识别每个要用到的变量，而函数调用中的Value实际上不会用的，所以不需要push
             */

            for (int i = 0; i < 3; i++) {
                if (i == params.size()) {
                    break;
                } else {
                    Register paramRegister = RegisterManager.instance().getParamRegister(i);
                    Value paramToPush = params.get(i);
                    if (paramToPush instanceof ConstNumber n) {
                        new Li(paramRegister, n.getVal());
                    } else {
                        Register register = RegisterManager.instance().getRegOf(paramToPush);
                        if (register != null &&
                                RegisterManager.instance().isUnassignedParamRegOrNormalReg(register, i)) {
                            //如果是尚未赋值的参数寄存器，则说明原值未被破坏，可以直接移过去
                            new Move(paramRegister, register);
                        } else {
                            //否则，要从栈上取值
                            new Lw(paramRegister, MipsManager.instance().getLocalVarAddr(paramToPush),
                                    RegisterManager.sp);
                        }
                    }
                    new Sw(paramRegister, MipsManager.instance().getStackPointer() - 4 * i, RegisterManager.sp);
                }
            }

            if (params.size() >= 4) {
                for (int i = 3; i < params.size(); i++) {
                    Register register = RegisterManager.k0;
                    if (params.get(i) instanceof ConstNumber n) {
                        new Li(register, n.getVal());
                    } else {
                        register = MipsManager.instance().getTempVarByRegister(params.get(i), RegisterManager.k0);
                        if (register != null && !RegisterManager.instance().isUnassignedParamRegOrNormalReg(register, 3)) {
                            register = RegisterManager.k0;
                            new Lw(register, MipsManager.instance().getLocalVarAddr(params.get(i)), RegisterManager.sp);
                        }
                    }
                    new Sw(register, MipsManager.instance().getStackPointer() - 4 * i, RegisterManager.sp);
                }
            }

            //移动$sp
            MipsManager.instance().saveSp();

            //3. 跳转
            new Jal(MipsManager.instance().getFunctionLabel(function));

            /*
                4. 返回，恢复现场
                    1) 保存返回值
                    2) 恢复当前函数参数
                    2) 恢复$ra
                    3) 移动$sp
             */
            //移动$sp
            MipsManager.instance().resetSp();
            //恢复$st
            if (Optimizer.instance().hasOptimized()) {
                for (int i = allocatedRegisters.size() - 1; i >= 0; i--) {
                    MipsManager.instance().popTo(allocatedRegisters.get(i));
                }
            }
            //恢复寄存器$ra
            MipsManager.instance().popTo(RegisterManager.ra);

            for (int i = 0; i < Math.min(curParamN, 3); i++) {
                //先load再说，deadcode优化以后做
                new Lw(RegisterManager.instance().getParamRegister(i), -4 * i, RegisterManager.sp);
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

    public boolean isUseless() {
        return !Optimizer.instance().hasSideEffect(getCallingFunction()) && userList.size() == 0;
    }

    public boolean isDefInstr() {
        Function function = (Function) operandList.get(0);
        return !function.isVoid();
    }

    public ArrayList<String> GVNHash() {
//        if (!Optimizer.instance().hasSideEffect(getCallingFunction()) &&
//                !useGlobalVar() && !Optimizer.instance().getFunctionOptimize().isRecursive(getCallingFunction())) {
//            //这里不用isUseless，因为不需要userList.size()=0，只要没有副作用就可以GVN
//            return super.GVNHash();
//        }
        return null;
    }

    public Function getCallingFunction() {
        return (Function) operandList.get(0);
    }

    public Integer toConst() {
        if (Optimizer.instance().hasSideEffect(getCallingFunction())) {
            //不能有副作用
            return null;
        }
        Function function = (Function) operandList.get(0);
        Integer retVal = null;
        if (!function.isVoid()) {
            for (BasicBlock block : function.getBlocks()) {
                for (Instruction instruction : block.getInstructionList()) {
                    if (instruction instanceof Ret ret) {

                        if (ret.getRetValue() instanceof ConstNumber n) {
                            //必须有且仅有一个返回常数值的ret语句(或返回值相同)
                            if (retVal == null || retVal == n.getVal()) {
                                retVal = n.getVal();
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return retVal;
    }

    private boolean useGlobalVar() {
        for (BasicBlock block : getCallingFunction().getBlocks()) {
            for (Instruction instruction : block.getInstructionList()) {
                for (Value operand : instruction.getOperandList()) {
                    if (operand instanceof GlobalDecl) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
