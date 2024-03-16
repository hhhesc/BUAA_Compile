package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Phi;
import IntermediatePresentation.Instruction.Putstr;
import IntermediatePresentation.Instruction.Ret;
import IntermediatePresentation.Instruction.Shift;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.Module;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import com.sun.tools.javac.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class FunctionOptimize {
    private final Module module;
    //由于没有函数声明，因此实际上是调用树
    private final HashMap<Function, ArrayList<Function>> functionCallTree = new HashMap<>();
    //具有副作用的函数
    private final HashSet<Function> sideEffectFunction = new HashSet<>();

    private final HashSet<Function> recursiveFunctions = new HashSet<>();

    //每个函数被调用的次数
    private final HashMap<Function, Integer> callCounter = new HashMap<>();


    public FunctionOptimize() {
        module = Optimizer.instance().getModule();
    }

    public void optimize() {
        int cnt = 0;
        for (Function f : module.getAllFunctions()) {
            ArrayList<Call> calls = new ArrayList<>();
            for (BasicBlock b : f.getBlocks()) {
                for (Instruction instr : b.getInstructionList()) {
                    if (instr instanceof Call) {
                        calls.add((Call) instr);
                    }
                }
            }

            for (Call call : calls) {
                cnt++;
                if (cnt > 15) {
                    break;
                }
                inline(call);
            }
            if (!(f instanceof MainFunction)) {
                Optimizer.instance().dominAnalyze(f);
            }
        }
        analyze();
    }

    public void analyze() {
        //包括调用分析和副作用分析
        functionCallTree.clear();
        sideEffectFunction.clear();
        recursiveFunctions.clear();
        callCounter.clear();

        for (Function f : module.getAllFunctions()) {
            for (BasicBlock block : f.getBlocks()) {
                for (Instruction instruction : block.getInstructionList()) {
                    if (instruction instanceof Putstr) {
                        sideEffectFunction.add(f);
                    } else if (instruction instanceof Call call) {
                        Function calledFunction = call.getCallingFunction();

                        //只有仅调用了一次，且不在循环中的才可以加入
                        if (block.getLoopDepth() == 0 && !callCounter.containsKey(calledFunction)) {
                            callCounter.put(calledFunction, 1);
                        } else {
                            callCounter.put(calledFunction, 2);
                        }

                        if (!functionCallTree.containsKey(f)) {
                            functionCallTree.put(f, new ArrayList<>());
                        }
                        functionCallTree.get(f).add(calledFunction);

                        if (hasSideEffect(calledFunction)) {
                            sideEffectFunction.add(f);
                        }

                        if (f.equals(calledFunction) || isRecursive(calledFunction)) {
                            recursiveFunctions.add(f);
                        }
                    } else if (instruction instanceof Store store && store.hasSideEffect()) {
                        sideEffectFunction.add(f);
                    }
                }
            }
        }

        HashSet<Function> functions = new HashSet<>(module.getFunctions());
        for (Function f : functions) {
            if (callCounter.getOrDefault(f, 0) == 0) {
                module.getFunctions().remove(f);
                f.destroy();
            }
        }
    }

    public boolean hasSideEffect(Function function) {
        return (sideEffectFunction.contains(function)) || (function.equals(Function.getint))
                || (function.equals(Function.putint));
    }

    public boolean isRecursive(Function function) {
        return recursiveFunctions.contains(function);
    }


    public ArrayList<Function> closurseWhenCall(Function function) {
        ArrayList<Function> closure = new ArrayList<>();
        LinkedList<Function> queue = new LinkedList<>();
        queue.add(function);

        while (!queue.isEmpty()) {
            Function f = queue.poll();
            if (closure.contains(f)) {
                continue;
            }
            closure.add(f);
            if (functionCallTree.containsKey(f) && functionCallTree.get(f) != null) {
                queue.addAll(functionCallTree.get(f));
            }
        }
        return closure;
    }

    public void inline(Call call) {
        Function calledFunction = call.getCallingFunction();
        BasicBlock b = call.getBlock();
        Function f = b.getFunction();
        if (isRecursive(calledFunction) || calledFunction.equals(Function.putint) ||
                calledFunction.equals(Function.getint)) {
            return;
        }

        BasicBlock afterCallBlock = new BasicBlock();
        f.addBlock(afterCallBlock);
        ArrayList<Instruction> instrs = new ArrayList<>(b.getInstructionList());
        int idx = b.getInstructionList().indexOf(call);
        for (int i = 0; i < instrs.size(); i++) {
            if (i > idx) {
                afterCallBlock.addInstruction(instrs.get(i));
            }
        }
        b.getInstructionList().removeAll(afterCallBlock.getInstructionList());

        b.removeInstruction(call);
        ArrayList<BasicBlock> newBlocks = functionColne(call, afterCallBlock);
        BasicBlock entry = newBlocks.get(0);

        b.addInstructionAt(idx, new Br(entry));

        for (BasicBlock block : newBlocks) {
            f.addBlock(block);
        }

        if (!calledFunction.isVoid()) {
            //phi
            call.beReplacedBy(afterCallBlock.getInstructionList().get(0));
        }
        call.destroy();
    }

    private ArrayList<BasicBlock> functionColne(Call call, BasicBlock afterBlock) {
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        //填充指令
        //原函数中的value和当前内联函数中的value
        Function calledFunction = call.getCallingFunction();
        HashMap<Value, Value> valMap = new HashMap<>();
        ArrayList<Value> formValues = calledFunction.getParam().getParams();

        Phi phi = null;
        if (!calledFunction.isVoid()) {
            phi = new Phi(new LocalDecl(), IRManager.getInstance().declareLocalVar());
            afterBlock.addInstrAtEntry(phi);
        }

        //加入形参->实参的对应
        for (int i = 0; i < formValues.size(); i++) {
            valMap.put(formValues.get(i), call.getOperandList().get(i + 1));
        }
        //加入block的对应
        ArrayList<BasicBlock> bfsDTArray = Optimizer.instance().bfsDominTreeArray(calledFunction.getEntranceBlock());
        for (BasicBlock block : calledFunction.getBlocks()) {
            BasicBlock curBlock = new BasicBlock();
            blocks.add(curBlock);
            valMap.put(block, curBlock);
        }

        HashSet<Phi> phiToBeFill = new HashSet<>();

        for (BasicBlock block : bfsDTArray) {
            //老问题：我如果向回跳入，而顺序遍历的话，会出现变量先使用后定义的情况，所以需要按支配树顺序遍历
            BasicBlock curBlock = (BasicBlock) valMap.get(block);

            for (Instruction instruction : block.getInstructionList()) {
                Instruction newInstruction = null;
                if (instruction instanceof ALU alu) {
                    newInstruction = new ALU(getVal(alu.getOperand1(), valMap),
                            alu.getOperator(), getVal(alu.getOperand2(), valMap));
                } else if (instruction instanceof Br br) {
                    if (br.getDest() == null) {
                        newInstruction = new Br(valMap.get(br.getCond()), (BasicBlock) valMap.get(br.getIfTrue()),
                                (BasicBlock) valMap.get(br.getIfFalse()));
                    } else {
                        newInstruction = new Br((BasicBlock) valMap.get(br.getDest()));
                    }
                } else if (instruction instanceof GetElementPtr gep) {
                    newInstruction = new GetElementPtr(getVal(gep.getPtr(), valMap),
                            getVal(gep.getElemIndex(), valMap));
                } else if (instruction instanceof Icmp icmp) {
                    newInstruction = new Icmp(icmp.getOperator(),
                            getVal(icmp.getOperandList().get(0), valMap),
                            getVal(icmp.getOperandList().get(1), valMap));
                } else if (instruction instanceof Load load) {
                    newInstruction = new Load(IRManager.getInstance().declareLocalVar(),
                            getVal(load.getAddr(), valMap));
                } else if (instruction instanceof LocalDecl alloca) {
                    if (valMap.containsKey(instruction)) {
                        newInstruction = (Instruction) valMap.get(instruction);
                    } else {
                        if (alloca.getType().equals(ValueType.PI32)) {
                            newInstruction = new LocalDecl();
                        } else {
                            newInstruction = new LocalDecl(alloca.getLen());
                        }
                    }
                } else if (instruction instanceof Phi aphi) {
                    newInstruction = new Phi((LocalDecl) valMap.get(aphi.getPhiAddr()),
                            IRManager.getInstance().declareLocalVar());
                    phiToBeFill.add(aphi);
                } else if (instruction instanceof Putstr putstr) {
                    newInstruction = new Putstr(putstr.getString(), putstr.getLen());
                } else if (instruction instanceof Ret ret) {
                    if (calledFunction.isVoid()) {
                        newInstruction = new Br(afterBlock);
                    } else {
                        newInstruction = new Br(afterBlock);
                        if (phi != null) {
                            phi.addCond(getVal(ret.getRetValue(), valMap), curBlock);
                        }
                    }
                } else if (instruction instanceof Shift shift) {
                    newInstruction = new Shift(shift.isShiftRight(),
                            getVal(shift.getOperandList().get(0), valMap),
                            (ConstNumber) shift.getOperandList().get(1));
                    ((Shift) newInstruction).setLogicalShiftRight(shift.isLogicalShiftRight());
                } else if (instruction instanceof Store store) {
                    newInstruction = new Store(getVal(store.getSrc(), valMap),
                            getVal(store.getAddr(), valMap));
                } else if (instruction instanceof ZextTo zextTo) {
                    newInstruction = new ZextTo(getVal(zextTo.getOperandList().get(0),
                            valMap), zextTo.getType());
                } else if (instruction instanceof Call call1) {
                    ArrayList<Value> rParams = new ArrayList<>();
                    for (Value para : call1.getOperandList()) {
                        if (!(para instanceof Function)) {
                            rParams.add(getVal(para, valMap));
                        }
                    }
                    newInstruction = new Call(call1.getCallingFunction(), rParams);
                }

                valMap.put(instruction, newInstruction);
                curBlock.addInstruction(newInstruction);
            }
        }

        //等到都完成了，再去做phi的填充，以防出现未定义的问题
        for (Phi originPhi : phiToBeFill) {
            Phi phiToFill = (Phi) valMap.get(originPhi);
            for (int i = 0; i < originPhi.getOperandList().size(); i += 2) {
                phiToFill.addCond(getVal(originPhi.getOperandList().get(i), valMap),
                        (BasicBlock) valMap.get(originPhi.getOperandList().get(i + 1)));
            }
        }
        return blocks;
    }

    private Value getVal(Value key, HashMap<Value, Value> valMap) {
        if (key instanceof ConstNumber || key instanceof GlobalDecl) {
            return key;
        } else {
            return valMap.get(key);
        }
    }

    public boolean calledOnce(Function function) {
        return callCounter.getOrDefault(function, 0) == 1;
    }
}
