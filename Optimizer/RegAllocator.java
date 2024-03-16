package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
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
import java.util.Objects;

public class RegAllocator {
    private final Module module;

    private final HashSet<Value> simplifyWorkList = new HashSet<>();
    private final HashSet<Value> freezeWorkList = new HashSet<>();
    private final HashSet<Value> spillWorkList = new HashSet<>();
    private final HashSet<Value> spilledNodes = new HashSet<>();
    private final HashSet<Value> coalescedNodes = new HashSet<>();
    private final HashSet<Value> coloredNodes = new HashSet<>();
    private final LinkedList<Value> selectStack = new LinkedList<>();

    private final HashSet<MoveIR> coalescedMoves = new HashSet<>();
    private final HashSet<MoveIR> constrainedMoves = new HashSet<>();
    private final HashSet<MoveIR> frozenMoves = new HashSet<>();
    private final HashSet<MoveIR> workListMoves = new HashSet<>();
    private final HashSet<MoveIR> activeMoves = new HashSet<>();

    private final HashSet<Edge> adjSet = new HashSet<>();
    private final HashMap<Value, HashSet<Value>> adjList = new HashMap<>();
    private final HashMap<Value, Integer> degree = new HashMap<>();
    private final HashMap<Value, HashSet<MoveIR>> moveList = new HashMap<>();
    private final HashMap<Value, Value> alias = new HashMap<>();


    private final ControlFlowGraph CFG;
    private final ArrayList<Register> registerPool = new ArrayList<>();

    private final HashMap<BasicBlock, HashSet<Value>> inValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> outValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> defValues = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<Value>> useValues = new HashMap<>();

    //在call执行之前的活跃变量集合
    private final HashMap<Call, HashSet<Value>> activeValueWhenCall = new HashMap<>();

    private Function curFunction;

    public RegAllocator() {
        this.module = Optimizer.instance().getModule();

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

    private void reset() {
        simplifyWorkList.clear();
        freezeWorkList.clear();
        spillWorkList.clear();
        spilledNodes.clear();
        coalescedNodes.clear();
        coloredNodes.clear();
        selectStack.clear();

        coalescedMoves.clear();
        constrainedMoves.clear();
        frozenMoves.clear();
        workListMoves.clear();
        activeMoves.clear();

        adjSet.clear();
        adjList.clear();
        degree.clear();
        moveList.clear();
        alias.clear();
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
        //TODO:这个嫌疑很大
        /*
            在基本块内部，也可以把每个指令看作一个基本块，上述活跃变量分析的公式依然适用
            从后向前遍历，def->去掉，use->加上，从而求出每个定义点处的active集合
            每次加入active集合时，都要将这些节点两两相连，也即active中这些节点构成完全图
        */

//        for (BasicBlock block : curFunction.getBlocks()) {
//            HashSet<Value> activeValues = new HashSet<>(outValues.get(block));
//            ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
//            for (int i = instructions.size() - 1; i >= 0; i--) {
//                Instruction instruction = instructions.get(i);
//                if (instruction instanceof MoveIR moveIR) {
//                    Phi dest = moveIR.getOriginPhi();
//                    Value src = moveIR.getSrc();
//
//                    if (notAllocated(src) && notAllocated(dest)) {
//                        activeValues.remove(src);
//
//                        moveList.putIfAbsent(src, new HashSet<>());
//                        moveList.get(src).add(moveIR);
//                        moveList.putIfAbsent(dest, new HashSet<>());
//                        moveList.get(dest).add(moveIR);
//
//                        workListMoves.add(moveIR);
//                    }
//                }
//
//
//                if (instruction.isDefInstr() && notAllocated(instruction)
//                        && !(instruction instanceof GlobalDecl) && !(instruction instanceof MoveIR)) {
//                    //active <- active \cup def(Instruction)
//                    //TODO:这里的def仅仅是其自身吗
//                    activeValues.add(instruction);
//
//                    for (Value v : activeValues) {
//                        addEdge(v, instruction);
//                    }
//                }
//
//                activeValues.remove(instruction);
//
//                for (Value operand : instruction.getOperandList()) {
//                    if (operand instanceof Instruction && !(operand instanceof GlobalDecl)) {
//                        activeValues.add(operand);
//                    }
//                }
//
//                if (instruction instanceof Call call) {
//                    activeValueWhenCall.put(call, new HashSet<>(activeValues));
//                }
//            }
//        }

        for (BasicBlock block : curFunction.getBlocks()) {
            HashSet<Value> activeValues = new HashSet<>(outValues.get(block));
            buildCompeleteMap(activeValues);
            ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
            for (int i = instructions.size() - 1; i >= 0; i--) {
                Instruction instruction = instructions.get(i);
                if (instruction.isDefInstr()) {
                    if (instruction instanceof MoveIR moveIR) {
                        Phi dest = moveIR.getOriginPhi();
                        Value src = moveIR.getSrc();
                        if (src instanceof ConstNumber) {
                            continue;
                        }
                        activeValues.remove(moveIR.getOriginPhi());

                        moveList.putIfAbsent(src, new HashSet<>());
                        moveList.get(src).add(moveIR);
                        moveList.putIfAbsent(dest, new HashSet<>());
                        moveList.get(dest).add(moveIR);

                        workListMoves.add(moveIR);
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
                addEdge(u, v);
            }
        }
    }

    private void addEdge(Value u, Value v) {
        if (!needAllocate(u) || !needAllocate(v) || u.equals(v)) {
            return;
        }
        if (!adjSet.contains(new Edge(u, v))) {
            adjSet.add(new Edge(u, v));
            adjSet.add(new Edge(v, u));

            if (notAllocated(u)) {
                adjList.putIfAbsent(u, new HashSet<>());
                adjList.get(u).add(v);
                degree.put(u, degree.getOrDefault(u, 0) + 1);
            }

            if (notAllocated(v)) {
                adjList.putIfAbsent(v, new HashSet<>());
                adjList.get(v).add(u);
                degree.put(v, degree.getOrDefault(v, 0) + 1);
            }
        }
    }

    private void makeWorkList() {
        for (BasicBlock b : curFunction.getBlocks()) {
            for (Value v : b.getInstructionList()) {
                if (!needAllocate(v)) {
                    continue;
                }
                if (degree.getOrDefault(v, 0) >= registerPool.size()) {
                    spillWorkList.add(v);
                } else if (moveRelated(v)) {
                    freezeWorkList.add(v);
                } else {
                    simplifyWorkList.add(v);
                }
            }
        }
    }

    private HashSet<Value> adjAcent(Value v) {
        HashSet<Value> adjListCpy = new HashSet<>(adjList.getOrDefault(v, new HashSet<>()));
        selectStack.forEach(adjListCpy::remove);
        coalescedNodes.forEach(adjListCpy::remove);
        return adjListCpy;
    }

    private boolean moveRelated(Value v) {
        return !nodeMoves(v).isEmpty();
    }

    private HashSet<MoveIR> nodeMoves(Value v) {
        HashSet<MoveIR> tempSet = new HashSet<>(activeMoves);
        tempSet.addAll(workListMoves);

        HashSet<MoveIR> moveListOfV = new HashSet<>(moveList.getOrDefault(v, new HashSet<>()));
        moveListOfV.retainAll(tempSet);
        return moveListOfV;
    }

    private void simplify() {
        Value n = simplifyWorkList.iterator().next();
        simplifyWorkList.remove(n);
        if (needAllocate(n)) {
            selectStack.push(n);
        }
        for (Value m : adjAcent(n)) {
            decrementDegree(m);
        }
    }

    private void decrementDegree(Value m) {
        int d = degree.get(m);
        degree.put(m, d - 1);
        if (d == registerPool.size()) {
            HashSet<Value> adjAcentM = new HashSet<>(adjAcent(m));
            adjAcentM.add(m);

            enableMoves(adjAcentM);
            spillWorkList.remove(m);

            if (moveRelated(m)) {
                freezeWorkList.add(m);
            } else {
                simplifyWorkList.add(m);
            }
        }
    }

    private void enableMoves(HashSet<Value> nodes) {
        for (Value n : nodes) {
            for (MoveIR m : nodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }

    private void coalesce() {
        MoveIR m = workListMoves.iterator().next();

        Value x = getAlias(m.getOriginPhi());
        Value y = getAlias(m.getSrc());

        if (!notAllocated(y)) {
            Value tmp = x;
            x = y;
            y = tmp;
        }

        workListMoves.remove(m);

        if (x.equals(y)) {
            coalescedMoves.add(m);
            addWorkList(x);
        } else if (!notAllocated(y) || adjSet.contains(new Edge(x, y))) {
            constrainedMoves.add(m);
            addWorkList(x);
            addWorkList(y);
        } else if (!notAllocated(x) && adjAcentOk(y, x) || notAllocated(x) &&
                conservativeAcent(x, y)) {
            coalescedMoves.add(m);
            combine(x, y);
            addWorkList(x);
        } else {
            activeMoves.add(m);
        }
    }

    private void combine(Value u, Value v) {
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v);
        } else {
            spillWorkList.remove(v);
        }

        coalescedNodes.add(v);
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));
        //enableMoves(v);
//        HashSet<Value> vSet = new HashSet();
//        vSet.add(v);
//        enableMoves(vSet);
        //TODO:这是要干啥？？？
        HashSet<Value> adjAcentPlus = new HashSet<>(adjList.getOrDefault(v,new HashSet<>()));
        adjAcentPlus.removeAll(coalescedNodes);
        for (Value t : adjAcent(v)) {
            //TODO:问题出现在这里，为什么是adjAcent而非全部adj呢
            addEdge(t, u);
            decrementDegree(t);
        }

        if (degree.getOrDefault(u, 0) >= registerPool.size() && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    private boolean adjAcentOk(Value v, Value u) {
        for (Value t : adjAcent(v)) {
            if (!ok(t, u)) {
                return false;
            }
        }
        return true;
    }

    private boolean conservativeAcent(Value u, Value v) {
        HashSet<Value> uAcent = new HashSet<>(adjAcent(u));
        uAcent.addAll(adjAcent(v));
        int k = 0;
        for (Value n : uAcent) {
            if (degree.getOrDefault(n, 0) >= registerPool.size()) {
                k++;
            }
        }
        return k < registerPool.size();
    }

    private boolean ok(Value t, Value r) {
        return degree.getOrDefault(t, 0) < registerPool.size() || !notAllocated(t) ||
                adjSet.contains(new Edge(t, r));
    }

    private void addWorkList(Value u) {
        if (notAllocated(u) && !moveRelated(u) && degree.getOrDefault(u, 0) < registerPool.size()) {
            freezeWorkList.remove(u);
            if (needAllocate(u)) {
                simplifyWorkList.add(u);
            }
        }
    }

    private Value getAlias(Value n) {
        if (coalescedNodes.contains(n)) {
            return getAlias(alias.get(n));
        } else {
            return n;
        }
    }

    private void freeze() {
        Value u = freezeWorkList.iterator().next();
        freezeWorkList.remove(u);
        if (needAllocate(u)) {
            simplifyWorkList.add(u);
        }
        freezeMoves(u);
    }

    private void freezeMoves(Value u) {
        for (MoveIR m : nodeMoves(u)) {
            Value v;

            v = getAlias(m.getOriginPhi()).equals(getAlias(u)) ?
                    getAlias(m.getSrc()) : getAlias(m.getOriginPhi());

            if (activeMoves.contains(m)) {
                //这部分是thra的代码
                activeMoves.remove(m);
            } else {
                workListMoves.remove(m);
            }

            frozenMoves.add(m);
            if (!moveRelated(v) && degree.getOrDefault(v, 0) < registerPool.size()) {
                freezeWorkList.remove(v);
                if (needAllocate(v)) {
                    simplifyWorkList.add(v);
                }
            }
        }
    }

    private void selectSpill() {
        Value m = null;
        final int useWt = 3;
        final int defWt = 3;
        final int cpyWt = 0;

        long weight;
        long minWeight = Integer.MAX_VALUE;
        Value minCostV = null;

        for (Value v : spillWorkList) {
            weight = 0;
            if (v instanceof Phi phi) {
                for (MoveIR move : phi.getMoveIrs()) {
                    weight += defWt * Math.pow(10, move.getBlock().getLoopDepth());
                }
            } else {
                weight += defWt * Math.pow(10, ((Instruction) v).getBlock().getLoopDepth());
            }

            for (User user : v.getUserList()) {
                if (weight >= minWeight) {
                    break;
                }
                if (user instanceof Instruction userInstr) {
                    if (userInstr instanceof MoveIR) {
                        weight -= cpyWt * Math.pow(10, userInstr.getBlock().getLoopDepth());
                    } else {
                        weight += useWt * Math.pow(10, userInstr.getBlock().getLoopDepth());
                    }
                }
            }

            if (weight < minWeight) {
                minWeight = weight;
                minCostV = v;
            }
        }

        if (minCostV != null) {
            m = minCostV;
        } else {
            for (Value v : spillWorkList) {
                m = v;
                break;
            }
        }


        if (needAllocate(m)) {
            simplifyWorkList.add(m);
        }
        freezeMoves(m);
        spillWorkList.remove(m);
    }

    private void assignColors() {
        while (!selectStack.isEmpty()) {
            Value n = selectStack.pop();
            HashSet<Register> registersToAllocate = new HashSet<>(registerPool);
            if (adjList.containsKey(n)) {
                for (Value w : adjList.get(n)) {
                    w = getAlias(w);
                    if (!notAllocated(w) || coloredNodes.contains(w)) {
                        registersToAllocate.remove(RegisterManager.instance().getRegOf(getAlias(w)));
                    }
                }
            }

            if (registersToAllocate.size() == 0) {
                spilledNodes.add(n);
            } else {
                coloredNodes.add(n);
                for (Register r : registersToAllocate) {
                    RegisterManager.instance().setRegOf(n, r);
                    break;
                }
            }
        }

        for (Value n : coalescedNodes) {
            if (needAllocate(n)) {
                RegisterManager.instance().setRegOf(n, RegisterManager.instance().getRegOf(getAlias(n)));
            }
        }
    }


    private class Edge {

        public Edge(Value u, Value v) {
            first = u;
            second = v;
        }

        public Value getFirst() {
            return first;
        }

        public void setFirst(Value first) {
            this.first = first;
        }

        private Value first;

        public Value getSecond() {
            return second;
        }

        public void setSecond(Value second) {
            this.second = second;
        }

        private Value second;

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (o instanceof Edge edge) {
                return first.equals(edge.getFirst()) && second.equals(edge.getSecond());
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(first, second);
        }

        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    private boolean notAllocated(Value v) {
        return RegisterManager.instance().getRegOf(v) == null;
    }

    public HashSet<Value> activeValuesWhenCall(Call call) {
        return activeValueWhenCall.getOrDefault(call, new HashSet<>());
    }

    private void printConflicts() {
        for (Edge edge : adjSet) {
            System.out.println(edge.getFirst().getReg() + " conflict with " + edge.getSecond().getReg());
        }
    }

    private boolean needAllocate(Value v) {
        return (v instanceof Instruction instr) && instr.isDefInstr() && !(v instanceof GlobalDecl) &&
                !(v instanceof MoveIR);
    }

    public void allocate() {
        for (Function f : module.getAllFunctions()) {
            curFunction = f;
            RegisterManager.instance().setCurFunction(curFunction);


            reset();
            blockLevelAnalyze();
            instructionLevelAnalyze();
//            printConflicts();
            makeWorkList();

            do {
                if (!simplifyWorkList.isEmpty()) {
                    simplify();
                } else if (!workListMoves.isEmpty()) {
                    coalesce();
                } else if (!freezeWorkList.isEmpty()) {
                    freeze();
                } else if (!spillWorkList.isEmpty()) {
                    selectSpill();
                }
            } while (!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty() ||
                    !spillWorkList.isEmpty());
            assignColors();
        }
    }

}
