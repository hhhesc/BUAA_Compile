package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.Instruction.Phi;
import IntermediatePresentation.Module;
import IntermediatePresentation.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ControlFlowGraph {
    private final HashMap<BasicBlock, HashSet<BasicBlock>> bbChildren = new HashMap<>();
    private final HashMap<BasicBlock, HashSet<BasicBlock>> bbParent = new HashMap<>();
    private final HashSet<BasicBlock> entrances = new HashSet<>();

    public void analyze(Module module) {
        for (Function function : module.getFunctions()) {
            analyseForFunction(function);
            entrances.add(function.getEntranceBlock());
        }
        analyseForFunction(module.getMainFunction());
        entrances.add(module.getMainFunction().getEntranceBlock());
    }

    public void analyze(Function function) {
        analyseForFunction(function);
        entrances.add(function.getEntranceBlock());
    }


    private void analyseForFunction(Function function) {
        for (BasicBlock block : function.getBlocks()) {
            Instruction lastInstr = block.getLastInstruction();

            if (lastInstr instanceof Br br) {
                BasicBlock dest = br.getDest();
                if (dest == null) {
                    BasicBlock ifTrue = br.getIfTrue();
                    BasicBlock ifFalse = br.getIfFalse();
                    addToMap(bbChildren, block, ifTrue);
                    addToMap(bbChildren, block, ifFalse);
                    addToMap(bbParent, ifTrue, block);
                    addToMap(bbParent, ifFalse, block);
                } else {
                    addToMap(bbChildren, block, dest);
                    addToMap(bbParent, dest, block);
                }
            }
        }
    }

    private void addToMap(HashMap<BasicBlock, HashSet<BasicBlock>> map, BasicBlock key, BasicBlock value) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        map.get(key).add(value);
    }

    public HashSet<BasicBlock> getEntrances() {
        return entrances;
    }

    public HashSet<BasicBlock> getParents(BasicBlock block) {
        return bbParent.getOrDefault(block, null);
    }

    public HashSet<BasicBlock> getChildren(BasicBlock block) {
        return bbChildren.getOrDefault(block, null);
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getChildrenGraph() {
        return bbChildren;
    }

    public HashSet<BasicBlock> reachedBlocks(Function f) {
        BasicBlock entry = f.getEntranceBlock();
        LinkedList<BasicBlock> queue = new LinkedList<>();
        HashSet<BasicBlock> visited = new HashSet<>();
        queue.add(entry);
        while (!queue.isEmpty()) {
            BasicBlock b = queue.poll();
            visited.add(b);


            if (bbChildren.containsKey(b) && bbChildren.get(b) != null) {
                for (BasicBlock child : bbChildren.get(b)) {
                    if (!visited.contains(child)) {
                        queue.add(child);
                    }
                }
            }
        }

        return visited;
    }

    public void removeBlock(BasicBlock block) {
        bbParent.remove(block);
        for (BasicBlock child : bbParent.keySet()) {
            if (bbParent.get(child) != null) {
                bbParent.get(child).remove(block);
            }
        }
        bbChildren.remove(block);
        for (BasicBlock parent : bbChildren.keySet()) {
            if (bbChildren.get(parent) != null) {
                bbChildren.get(parent).remove(block);
            }
        }
    }

    public ArrayList<BasicBlock> reverseBfsBlocksOf(Function f) {
        //对f中的基本块逆控制流bfs得到遍历的block序列,用以活跃变量分析
        LinkedList<BasicBlock> stack = new LinkedList<>();
        BasicBlock entrance = f.getEntranceBlock();
        HashSet<BasicBlock> visited = new HashSet<>();
        LinkedList<BasicBlock> queue = new LinkedList<>();
        queue.add(entrance);
        stack.push(entrance);

        while (!queue.isEmpty()) {
            BasicBlock block = queue.poll();
            if (visited.contains(block)) {
                continue;
            }
            visited.add(block);

            if (bbChildren.containsKey(block)) {
                for (BasicBlock child : bbChildren.get(block)) {
                    //如果可以从更靠后的块到达，就应该将其后移
                    stack.remove(child);
                    stack.push(child);
                    queue.add(child);
                }
            }
        }

        return new ArrayList<>(stack);
    }

    public boolean mayUseValueTilMove(Instruction v, MoveIR moveIR) {
        //从v经过任何路径直到move指令处，是否使用过phi
        BasicBlock vBlock = v.getBlock();
        BasicBlock moveBlock = moveIR.getBlock();
        Phi phi = moveIR.getOriginPhi();
        HashSet<BasicBlock> blocksWithUserIn = new HashSet<>();
        for (User user : phi.getUserList()) {
            BasicBlock userBlock = user.getBlock();
            if (user.equals(moveIR) || user.equals(v)) {
                //在move或者v处使用是没关系的
                continue;
            }

            if (userBlock.equals(moveBlock)) {
                //只能在其之后被使用
                if (userBlock.getInstructionList().indexOf(user) < userBlock.getInstructionList().indexOf(moveIR)) {
                    return true;
                }
                continue;
            }

            if (userBlock.equals(vBlock)) {
                //如果和v在同一个块中
                //TODO:这里可以优化吗？
                return true;
            }

            //在其中的block一定不是vB或mvB
            blocksWithUserIn.add(userBlock);
        }

        if (blocksWithUserIn.size() == 0) {
            return false;
        }

        //dfs，如果可以到达moveBlcok，那么就会使用
        HashSet<BasicBlock> reachedBlocks = new HashSet<>();
        mayReach(vBlock, moveBlock, reachedBlocks);

        reachedBlocks.retainAll(blocksWithUserIn);
        //如果二者有交集，说明可能在move之前被使用，不能合并
        return reachedBlocks.size() != 0;
    }

    private void mayReach(BasicBlock src, BasicBlock dest, HashSet<BasicBlock> visited) {
        //直到遇到move语句之前，可能去到哪些块
        if (visited.contains(src) || src.equals(dest)) {
            return;
        }
        visited.add(src);

        if (!bbChildren.containsKey(src) || bbChildren.get(src) == null || bbChildren.get(src).size() == 0) {
            return;
        }

        for (BasicBlock child : bbChildren.get(src)) {
            mayReach(child, dest, visited);
        }
    }

    public HashSet<BasicBlock> mayPassingBy(BasicBlock src, BasicBlock dest, HashSet<BasicBlock> visited) {
        if (visited.contains(src)) {
            //如果已经访问过，说明这条路不对，返回null
            return null;
        }
        visited.add(src);
        HashSet<BasicBlock> mayReach = new HashSet<>();

        if (src.equals(dest)) {
            //到达了终点
            mayReach.add(src);
            return mayReach;
        }

        if (!bbChildren.containsKey(src) || bbChildren.get(src) == null || bbChildren.get(src).size() == 0) {
            //到达了末尾节点，不可达了
            return null;
        }

        for (BasicBlock child : bbChildren.get(src)) {
            HashSet<BasicBlock> nextBlocks = mayPassingBy(child, dest, visited);
            if (nextBlocks != null) {
                //不为空，说明可达
                mayReach.addAll(nextBlocks);
            }
        }

        if (mayReach.size() == 0) {
            return null;
        } else {
            //如果可达，就要把自己加上
            mayReach.add(src);
            return mayReach;
        }
    }

    public void addMidBlock(BasicBlock prev, BasicBlock next, BasicBlock mid) {
        bbChildren.get(prev).remove(next);
        bbChildren.get(prev).add(mid);
        bbParent.get(next).remove(prev);
        bbParent.get(next).add(mid);

        bbParent.put(mid, new HashSet<>());
        bbChildren.put(mid, new HashSet<>());
        bbParent.get(mid).add(prev);
        bbChildren.get(mid).add(next);
    }
}
