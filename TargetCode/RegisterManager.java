package TargetCode;

import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Value;
import Optimizer.Optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.CRC32;

public class RegisterManager {
    /*
        1. 寄存器使用情况记录
        2. 寄存器分配
     */

    private static final RegisterManager INSTANCE = new RegisterManager();
    private final HashMap<Function, HashMap<Value, Register>> regOfVal = new HashMap<>();

    private Function curFunction;

    public static final Register zero = new Register("$zero");
    public static final Register at = new Register("$at");
    public static final Register v0 = new Register("$v0");
    public static final Register v1 = new Register("$v1");
    public static final Register a0 = new Register("$a0");
    public static final Register a1 = new Register("$a1");
    public static final Register a2 = new Register("$a2");
    public static final Register a3 = new Register("$a3");
    public static final Register t0 = new Register("$t0");
    public static final Register t1 = new Register("$t1");
    public static final Register t2 = new Register("$t2");
    public static final Register t3 = new Register("$t3");
    public static final Register t4 = new Register("$t4");
    public static final Register t5 = new Register("$t5");
    public static final Register t6 = new Register("$t6");
    public static final Register t7 = new Register("$t7");
    public static final Register s0 = new Register("$s0");
    public static final Register s1 = new Register("$s1");
    public static final Register s2 = new Register("$s2");
    public static final Register s3 = new Register("$s3");
    public static final Register s4 = new Register("$s4");
    public static final Register s5 = new Register("$s5");
    public static final Register s6 = new Register("$s6");
    public static final Register s7 = new Register("$s7");
    public static final Register t8 = new Register("$t8");
    public static final Register t9 = new Register("$t9");
    public static final Register k0 = new Register("$k0");
    public static final Register k1 = new Register("$k1");
    public static final Register gp = new Register("$gp");
    public static final Register sp = new Register("$sp");
    public static final Register fp = new Register("$fp");
    public static final Register ra = new Register("$ra");

    private RegisterManager() {
    }

    public static RegisterManager instance() {
        return INSTANCE;
    }

    public Register getRegOf(Value v) {
        //这里从当前的map中取得
        Register register = regOfVal.get(curFunction).getOrDefault(v, null);
        if (register == null || register.equals(k0) || register.equals(k1)) {
            return null;
        } else {
            return register;
        }
    }

    public void setRegOf(Value v, Register register) {
        //默认该寄存器已经被free过了或者将要被free
//        System.out.println("set " + v.getReg() + " with " + register);
        regOfVal.get(curFunction).put(v, register);
    }

    public Register getParamRegister(int index) {
        return switch (index) {
            case 0 -> a1;
            case 1 -> a2;
            case 2 -> a3;
            default -> null;
        };
    }


    public boolean isUnassignedParamRegOrNormalReg(Register register, int i) {
        boolean isParamReg = register.equals(a1) || register.equals(a2) || register.equals(a3);
        if (!isParamReg) {
            return true;
        } else {
            if (i == 0) {
                return true;
            } else if (i == 1) {
                return !register.equals(a1);
            } else if (i == 2) {
                return register.equals(a3);
            }
        }
        return false;
    }

    public void setCurFunction(Function function) {
        curFunction = function;
        if (!regOfVal.containsKey(function)) {
            regOfVal.put(function, new HashMap<>());
        }
    }

    public ArrayList<Register> activeRegistersWhenCall(Call call) {
        HashSet<Value> activeValues = Optimizer.instance().activeValuesWhenCall(call);
        HashSet<Register> activeRegisters = new HashSet<>();
        HashMap<Value, Register> regOfValue = regOfVal.get(curFunction);
        for (Value value : activeValues) {
            if (regOfValue.containsKey(value) && regOfValue.get(value) != null) {
                activeRegisters.add(regOfValue.get(value));
            }
        }
        return new ArrayList<>(activeRegisters);
    }

    public HashSet<Register> shouldSaveRegsWhenCall(Function function) {
        if (!Optimizer.instance().hasOptimized()) {
            return new HashSet<>();
        }
        ArrayList<Function> callClosure = Optimizer.instance().closurseWhenCall(function);
        HashSet<Register> ret = new HashSet<>();
        for (Function f : callClosure) {
            if (regOfVal.get(f) != null) {
                ret.addAll(new HashSet<>(regOfVal.get(f).values()));
            }
        }
        return ret;
    }
}
