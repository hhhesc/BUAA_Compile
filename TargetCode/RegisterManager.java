package TargetCode;

import IntermediatePresentation.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterManager {
    /*
        1. 寄存器使用情况记录
        2. 寄存器分配

        直至目前，先不考虑寄存器的分配
        暂时先用学长的思路，只留下k0和k1作临时寄存器，即四元式的两个操作数，以k0作为dest
        临时寄存器保证在一个llvm四元式内部不会破坏，如果有临时变量需要使用，那么现取现用，用完销毁
        以t0调用存储在内存中的临时变量，用完销毁
        也即一个llvm四元式解析过程中，只能k0和k1作为操作数，以t0作为存储在内存中的临时变量临时调取的寄存器
     */

    private static final RegisterManager INSTANCE = new RegisterManager();
    private final HashMap<Value, Register> registerOfValue = new HashMap<>();

    private final ArrayList<Register> tempRegisters = new ArrayList<>();
    private final ArrayList<Register> storageRegisters = new ArrayList<>();

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
        tempRegisters.add(t0);
        tempRegisters.add(t1);
        tempRegisters.add(t2);
        tempRegisters.add(t3);
        tempRegisters.add(t4);
        tempRegisters.add(t5);
        tempRegisters.add(t6);
        tempRegisters.add(t7);
        tempRegisters.add(t8);
        tempRegisters.add(t9);
        storageRegisters.add(s0);
        storageRegisters.add(s1);
        storageRegisters.add(s2);
        storageRegisters.add(s3);
        storageRegisters.add(s4);
        storageRegisters.add(s5);
        storageRegisters.add(s6);
        storageRegisters.add(s7);
    }

    public static RegisterManager instance() {
        return INSTANCE;
    }

    public Register getRegOf(Value v) {
        return registerOfValue.getOrDefault(v, null);
    }

    public void setRegOf(Value v, Register register) {
        registerOfValue.put(v, register);
    }

    public Register getParamRegister(int index) {
        return switch (index) {
            case 0 -> a0;
            case 1 -> a1;
            case 2 -> a2;
            case 3 -> a3;
            default -> null;
        };
    }
}
