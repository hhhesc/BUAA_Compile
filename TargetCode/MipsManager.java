package TargetCode;

import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Value;
import TargetCode.Instruction.ALU.Addi;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;

import java.util.HashMap;
import java.util.LinkedList;

public class MipsManager {
    /*
        1. 存储空间管理
        2. 全局数据管理
     */
    private static final MipsManager INSTANCE = new MipsManager();
    private static final MipsFile FILE = new MipsFile();

    private final HashMap<Value, String> irToGlobalData = new HashMap<>();
    private final HashMap<Value, Integer> valueToStackOffset = new HashMap<>();

    private final LinkedList<Integer> spOffsetStack = new LinkedList<>();

    private final HashMap<Function, Label> functionLabels = new HashMap<>();


    private int stackPointer = 0;

    private MipsManager() {
    }

    public static MipsManager instance() {
        return INSTANCE;
    }

    public static MipsFile getFile() {
        return FILE;
    }

    public void declGlobalData(Value v, String s) {
        irToGlobalData.put(v, s);
    }

    public String getGlobalData(Value v) {
        return irToGlobalData.get(v);
    }

    public void allocInStackBy(Value v, int len) {
        /*
            这里是为了和全局变量的存放方向一致，即第k个元素的位置是sp + offset * 4 * (k-1)
         */
        stackPointer -= 4 * (len - 1);
        valueToStackOffset.put(v, stackPointer);
        stackPointer -= 4;
    }

    public int getLocalVarAddr(Value v) {
        return valueToStackOffset.get(v);
    }

    public void pushTempVar(Value v, Register tempRegister) {
        new Sw(tempRegister, stackPointer, RegisterManager.sp);
        valueToStackOffset.put(v, stackPointer);
        stackPointer -= 4;
    }

    public Register getTempVarByRegister(Value v, Register defaultReg) {
        //将临时变量以寄存器的形式传递并销毁，该寄存器用完即销毁
        Register register = RegisterManager.instance().getRegOf(v);
        if (register == null) {
            //如果没有分配寄存器，就用默认的defaultReg
            register = defaultReg;
            //临时变量一定已经存储在了堆栈里
            int offset = valueToStackOffset.get(v);
            new Lw(register, offset, RegisterManager.sp);
            //似乎没办法释放内存碎片...
        }
        return register;
    }

    public void putDeclaredVarIntoRegister(Value v, Register register) {
        //存储到对应寄存器中
        if (v instanceof GlobalDecl) {
            new La(RegisterManager.k0, getGlobalData(v));
            new Lw(register, 0, RegisterManager.k0);
        } else {
            //临时变量或第五个及以上的参数
            new Lw(register, getLocalVarAddr(v), RegisterManager.sp);
        }
    }

    public void pushValue(Value v) {
        valueToStackOffset.put(v, stackPointer);
        stackPointer -= 4;
    }

    public void pointToSameMemory(Value src, Value tar) {
        valueToStackOffset.put(tar, valueToStackOffset.get(src));
    }

    public void tagBBWithLabel(Label label) {
        FILE.tagBBWithLabel(label);
    }

    public void tagFunctionWithLabel(Function function, Label label) {
        functionLabels.put(function, label);
    }

    public Label getFunctionLabel(Function function) {
        return functionLabels.get(function);
    }

    public void insertLabel(Label label) {
        FILE.insertLabel(label);
    }

    public void push(Register register) {
        new Sw(register, stackPointer, RegisterManager.sp);
        stackPointer -= 4;
    }

    public void allocEmptyStackSpace() {
        stackPointer -= 4;
    }

    public void popTo(Register dest) {
        stackPointer += 4;
        new Lw(dest, stackPointer, RegisterManager.sp);
    }

    public void saveSp(int paramNumbers) {
        //$sp的正常值就是栈顶，返回值也是
        //参数本来就是下面栈中的一部分，因此要为其预留位置
        spOffsetStack.push(stackPointer + 4 * paramNumbers);
        new Addi(RegisterManager.sp, RegisterManager.sp, stackPointer + 4 * paramNumbers);
        stackPointer = 0;
    }

    public void resetSp() {
        if (spOffsetStack.size() > 0) {
            stackPointer = spOffsetStack.pop();
            new Addi(RegisterManager.sp, RegisterManager.sp, -stackPointer);
        } else {
            stackPointer = 0;
        }
    }
}
