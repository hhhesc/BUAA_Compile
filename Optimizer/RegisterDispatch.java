package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.Instruction.Phi;
import IntermediatePresentation.Module;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegisterDispatch {
    private final HashMap<BasicBlock, HashSet<Value>> inValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> outValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> defValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> useValues = new HashMap<>();
    private final HashMap<Value, HashSet<Value>> conflictMapCopy = new HashMap<>();

    private final HashMap<Value, HashSet<Value>> conflictMap = new HashMap<>();
    private final LinkedList<Value> conflictMapValueStack = new LinkedList<>();

    //在call执行之前的活跃变量集合
    private final HashMap<Call, HashSet<Value>> activeValueWhenCall = new HashMap<>();

    private final Module module;
    private final ControlFlowGraph CFG;
    private final ArrayList<Register> registerPool = new ArrayList<>();

    private final HashMap<Phi, ArrayList<Value>> mergedMoves = new HashMap<>();

    private final HashMap<Phi, HashSet<Value>> moveRelated = new HashMap<>();

    private final HashSet<MoveIR> movesToMerge = new HashSet<>();

    private Function curFunction;

    public RegisterDispatch() {
        module = Optimizer.instance().getModule();
        CFG = Optimizer.instance().getCFG();
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock b : f.getBlocks()) {
                inValues.put(b, new HashSet<>());
                outValues.put(b, new HashSet<>());
                defValues.put(b, new HashSet<>());
                useValues.put(b, new HashSet<>());
            }
        }

        registerPool.add(RegisterManager.t0);
        registerPool.add(RegisterManager.t1);
        registerPool.add(RegisterManager.t2);
        registerPool.add(RegisterManager.t3);
        registerPool.add(RegisterManager.t4);
        registerPool.add(RegisterManager.t5);
        registerPool.add(RegisterManager.t6);
        registerPool.add(RegisterManager.t7);
        registerPool.add(RegisterManager.t8);
        registerPool.add(RegisterManager.t9);
        registerPool.add(RegisterManager.s0);
        registerPool.add(RegisterManager.s1);
        registerPool.add(RegisterManager.s2);
        registerPool.add(RegisterManager.s3);
        registerPool.add(RegisterManager.s4);
        registerPool.add(RegisterManager.s5);
        registerPool.add(RegisterManager.s6);
        registerPool.add(RegisterManager.s7);
        registerPool.add(RegisterManager.fp);
        registerPool.add(RegisterManager.gp);
        registerPool.add(RegisterManager.v1);
    }


    public void dispatch() {
        for (Function f : module.getAllFunctions()) {
            curFunction = f;
            RegisterManager.instance().setCurFunction(curFunction);
            blockLevelAnalyze();
            instructionLevelAnalyze();

            do {
                while (!simplify()) {
                    coalesce();
                }

                if (!movesToMerge.isEmpty()) {
                    freeze();
                } else {
                    spill();
                }
            } while (!conflictMap.isEmpty());
            select();

            conflictMap.clear();
            conflictMapCopy.clear();
            conflictMapValueStack.clear();
            movesToMerge.clear();
            moveRelated.clear();
        }
    }

    private void blockLevelAnalyze() {
        boolean hasChanged;
        do {
            hasChanged = false;
            ArrayList<BasicBlock> reverseList = CFG.reverseBfsBlocksOf(curFunction);

            for (BasicBlock b : reverseList) {
                //先求出use和def来 use先使用后定义；def先定义后使用
                HashSet<Value> defOfB = defValues.get(b);
                HashSet<Value> useOfB = useValues.get(b);

                for (Instruction instruction : b.getInstructionList()) {
                    if (instruction instanceof MoveIR moveIR) {
                        if (!moveRelated.containsKey(moveIR.getOriginPhi())) {
                            moveRelated.put(moveIR.getOriginPhi(), new HashSet<>());
                        }
                        moveRelated.get(moveIR.getOriginPhi()).add(moveIR.getOperandList().get(0));
                        movesToMerge.add(moveIR);
                    }

                    if (instruction.isDefInstr() && !useOfB.contains(instruction)) {
                        if (instruction instanceof MoveIR moveIR) {
                            defOfB.add(moveIR.getOriginPhi());
                        } else {
                            defOfB.add(instruction);
                        }
                    }
                    for (Value operand : instruction.getOperandList()) {
                        if (!defOfB.contains(operand) && operand instanceof Instruction instr) {
                            //不需要判断move，因为这里只会用到phi而不会用到move的value
                            useOfB.add(instr);
                        }
                    }
                }
            }

            for (BasicBlock b : reverseList) {
                //out[B] = \cup_{children} in[child]
                HashSet<BasicBlock> nextBlocks = CFG.getChildren(b);
                if (nextBlocks != null && nextBlocks.size() != 0) {
                    for (BasicBlock nextBlock : nextBlocks) {
                        outValues.get(b).addAll(inValues.get(nextBlock));
                    }
                }

                //in[B] = use[B] \cup (out[B] - def[B])
                HashSet<Value> originInValues = new HashSet<>(inValues.get(b));
                HashSet<Value> set = new HashSet<>(outValues.get(b));
                set.removeAll(defValues.get(b));

                inValues.get(b).addAll(set);
                inValues.get(b).addAll(useValues.get(b));
                if (!originInValues.equals(inValues.get(b))) {
                    hasChanged = true;
                }
            }
        } while (hasChanged);
    }

    private void instructionLevelAnalyze() {
        /*
            在基本块内部，也可以把每个指令看作一个基本块，上述活跃变量分析的公式依然适用
            从后向前遍历，def->去掉，use->加上，从而求出每个定义点处的active集合
            每次加入active集合时，都要将这些节点两两相连，也即active中这些节点构成完全图
        */

        for (BasicBlock block : curFunction.getBlocks()) {
            HashSet<Value> activeValues = new HashSet<>(outValues.get(block));
            buildCompeleteMap(activeValues);
            ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
            for (int i = instructions.size() - 1; i >= 0; i--) {
                Instruction instruction = instructions.get(i);
                if (instruction.isDefInstr()) {
                    if (instruction instanceof MoveIR moveIR) {
                        activeValues.remove(moveIR.getOriginPhi());
                    } else {
                        activeValues.remove(instruction);
                    }
                }

                for (Value operand : instruction.getOperandList()) {
                    if (operand instanceof Instruction) {
                        activeValues.add(operand);
                    }
                }
                buildCompeleteMap(activeValues);
                if (instruction instanceof Call call) {
                    activeValueWhenCall.put(call, new HashSet<>(activeValues));
                }
            }
        }
    }

    private void buildCompeleteMap(HashSet<Value> activeSet) {
        for (Value value : activeSet) {
            if (!conflictMap.containsKey(value)) {
                conflictMap.put(value, new HashSet<>());
                conflictMapCopy.put(value, new HashSet<>());
            }
        }
        for (Value v : activeSet) {
            if (!(v instanceof Instruction) || v instanceof GlobalDecl ||
                    RegisterManager.instance().getRegOf(v) != null) {
                continue;
            }
            for (Value u : activeSet) {
                if (!(u instanceof Instruction) || u instanceof GlobalDecl ||
                        RegisterManager.instance().getRegOf(v) != null || u.equals(v)) {
                    continue;
                }
                if (!conflictMap.get(u).contains(v) && !u.equals(v)) {
                    conflictMap.get(u).add(v);
                    conflictMap.get(v).add(u);
                    conflictMapCopy.get(u).add(v);
                    conflictMapCopy.get(v).add(u);
                }
            }
        }
    }

    private boolean simplify() {

        int K = registerPool.size();

        int exeCnt = 0;

        boolean hasChanged;
        do {
            exeCnt++;
            hasChanged = false;
            HashSet<Value> keys = new HashSet<>(conflictMap.keySet());
            HashSet<Value> moveRelatedValues = new HashSet<>(moveRelated.keySet());
            for (HashSet<Value> values : moveRelated.values()) {
                moveRelatedValues.addAll(values);
            }

            for (Value v : keys) {
                //如果move-related，那先不要入栈
                if (conflictMap.get(v).size() < K && !moveRelatedValues.contains(v)) {
                    removeFromConflictMap(v);
                    conflictMapValueStack.push(v);
                    hasChanged = true;
                    break;
                }
            }
        } while (hasChanged);

        return exeCnt == 1;
    }

    private void removeFromConflictMap(Value v) {
        ArrayList<Value> conflictValues = new ArrayList<>(conflictMap.get(v));
        for (Value neighbor : conflictValues) {
            if (conflictMap.containsKey(neighbor) && conflictMap.get(neighbor) != null) {
                conflictMap.get(neighbor).remove(v);
            }
        }
        conflictMap.remove(v);
    }

    private void coalesce() {
        HashSet<MoveIR> movesToMergeIt = new HashSet<>(movesToMerge);
        for (MoveIR moveIR : movesToMergeIt) {
            Value src = moveIR.getOperandList().get(0);
            Phi phi = moveIR.getOriginPhi();

            if (!(src instanceof Instruction instruction)) {
                movesToMerge.remove(moveIR);
                continue;
            }
            //由于GCM，需要考虑，从src的定义点处直到move处所可能经历的所有指令，都不能使用src作为操作数；否则就不能合并
            if (Optimizer.instance().getCFG().mayUseValueTilMove(instruction, moveIR)) {
                movesToMerge.remove(moveIR);
                continue;
            }

            //合并之后，度数>=K的结点数不会增加
            if (canMerge(phi, src)) {
//                System.out.println("merge " + phi.getReg() + " " + src.getReg());
                Value v1 = phi;
                Value v2 = src;
                if (RegisterManager.instance().getRegOf(src) != null) {
                    v1 = src;
                    v2 = phi;
                }
                //把v2合并到v1

                conflictMap.get(v1).addAll(conflictMap.get(v2));
                conflictMapCopy.get(v1).addAll(conflictMapCopy.get(v2));
                conflictMap.get(v1).remove(v2);
                conflictMapCopy.get(v1).remove(v2);

                HashSet<Value> neighbors = new HashSet<>(conflictMap.get(v2));
                for (Value neighbor : neighbors) {
                    if (conflictMap.containsKey(neighbor)) {
                        conflictMap.get(neighbor).add(v1);
                        conflictMapCopy.get(neighbor).add(v1);
                        conflictMap.get(neighbor).remove(v2);
                        conflictMapCopy.get(neighbor).remove(v2);
                    }
                }
                conflictMap.remove(v2);
                conflictMapCopy.remove(v2);

                //在分配寄存器后，为move的两个操作数设置相同的寄存器
                if (!mergedMoves.containsKey(phi)) {
                    mergedMoves.put(phi, new ArrayList<>());
                }
                mergedMoves.get(phi).add(src);
            }
        }
    }

    private boolean canMerge(Value v1, Value v2) {
        if (conflictMap.containsKey(v1) && conflictMap.containsKey(v2)) {
            HashSet<Value> v1Neighbors = new HashSet<>(conflictMap.get(v1));
            v1Neighbors.addAll(conflictMap.get(v2));
            return v1Neighbors.size() < registerPool.size();
        } else {
            //如果有一个不在冲突图里，那也就没有什么合并可言了
            return false;
        }
    }

    private void freeze() {
        MoveIR move = null;
        for (MoveIR moveIR : movesToMerge) {
            move = moveIR;
        }

        movesToMerge.remove(move);
    }

    private void spill() {
        //随机选择一个节点，将其移走，重复简化
        //或者选择一个cost最小的节点，cost=usewt*(\sum 10^useDepth)+defwt*(\sum 10^defDepth)-copywt*(\sum 10^copyDepth)
        double weight;
        double minWeight = Double.MAX_VALUE;
        double maxWeight = 0;
        Value bestV = null;
        final double alpha = 10;

        for (Value v : conflictMap.keySet()) {
            weight = valueWeight(v);
            for (Value u : conflictMap.get(v)) {
                weight -= valueWeight(u);
            }

            if (weight < minWeight) {
                minWeight = weight;
                bestV = v;
            }
        }

        if (bestV != null) {
            removeFromConflictMap(bestV);
            conflictMapValueStack.push(bestV);
        } else if (!conflictMap.isEmpty()) {
            for (Value v : conflictMap.keySet()) {
                removeFromConflictMap(v);
                conflictMapValueStack.push(v);
                break;
            }
        }
    }

    private double valueWeight(Value v) {
        final int useWt = 1;
        final int defWt = 1;
        final double alpha = 10;

        double weight = 0;
        if (v instanceof Phi phi) {
            for (MoveIR move : phi.getMoveIrs()) {
                weight += defWt * Math.pow(alpha, move.getBlock().getLoopDepth());
            }
        } else {
            weight += defWt * Math.pow(alpha, ((Instruction) v).getBlock().getLoopDepth());
        }

        for (User user : v.getUserList()) {
            if (weight < 0) {
                break;
            }
            if (user instanceof Instruction userInstr) {
                weight += useWt * Math.pow(alpha, userInstr.getBlock().getLoopDepth());
            }
        }
//        System.out.println(v.getReg() + ":" + weight);
        return weight;
    }

    private void select() {
        //添加，并着色
        while (!conflictMapValueStack.isEmpty()) {
            Value v = conflictMapValueStack.pop();
            //指定一个与所有邻接点的颜色不同的颜色
            if (RegisterManager.instance().getRegOf(v) != null) {
                continue;
            }
            HashSet<Register> neighborRegs = new HashSet<>();
            for (Value neighbor : conflictMapCopy.get(v)) {
                if (RegisterManager.instance().getRegOf(neighbor) != null) {
                    neighborRegs.add(RegisterManager.instance().getRegOf(neighbor));
                }
            }

            if (neighborRegs.size() < registerPool.size()) {
                //如果邻接点的颜色数小于等于K，则可以分配
                for (Register r : registerPool) {
                    if (!neighborRegs.contains(r)) {
                        RegisterManager.instance().setRegOf(v, r);
                        if (v instanceof Phi phi) {
                            setRegInMergeMap(phi, r, new HashSet<>());
                        }
                        break;
                    }
                }
            }
        }
    }

    public HashSet<Value> activeValuesWhenCall(Call call) {
        return activeValueWhenCall.getOrDefault(call, new HashSet<>());
    }

    public void setRegInMergeMap(Phi phi, Register r, HashSet<Phi> visited) {
        if (!mergedMoves.containsKey(phi) || visited.contains(phi)) {
            return;
        }
        visited.add(phi);
        RegisterManager.instance().setRegOf(phi, r);
        for (Value moveSrc : mergedMoves.get(phi)) {
            RegisterManager.instance().setRegOf(moveSrc, r);
            for (Phi relatedPhi : mergedMoves.keySet()) {
                if (!phi.equals(relatedPhi) && mergedMoves.get(relatedPhi).contains(moveSrc)) {
                    setRegInMergeMap(relatedPhi, r, visited);
                }
            }
        }
    }
}
