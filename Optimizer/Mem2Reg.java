package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.Instruction.Phi;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Module;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Mem2Reg {
    private DominAnalyzer dominAnalyzer;
    private Module module;

    public Mem2Reg() {
        this.dominAnalyzer = Optimizer.instance().getDominAnalyzer();
        this.module = Optimizer.instance().getModule();
    }


    public void optimize(Module module) {

        //插入Phi
        insertPhiLoop();
        //变量重命名
        for (Function function : module.getAllFunctions()) {
            rename(function.getEntranceBlock(), new HashMap<>());
        }

    }

    private void insertPhiLoop() {
        //只有LocalDecl指令，即Alloca会产生局部变量
        for (Function function : module.getFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                ArrayList<Instruction> blockInstructions = new ArrayList<>(block.getInstructionList());
                for (Instruction instruction : blockInstructions) {
                    if (instruction instanceof LocalDecl localDecl && localDecl.getType() == ValueType.PI32) {
                        insertPhi(localDecl);
                    }
                }
            }
        }

        for (BasicBlock block : module.getMainFunction().getBlocks()) {
            ArrayList<Instruction> blockInstructions = new ArrayList<>(block.getInstructionList());
            for (Instruction instruction : blockInstructions) {
                if (instruction instanceof LocalDecl localDecl && localDecl.getType() == ValueType.PI32) {
                    insertPhi(localDecl);
                }
            }
        }
    }

    private void insertPhi(LocalDecl v) {
        //将要增加phi指令的块
        LinkedList<BasicBlock> listF = new LinkedList<>();
        //v的定义集合
        LinkedList<BasicBlock> listW = new LinkedList<>();

        LinkedList<BasicBlock> defs = new LinkedList<>();

        for (Value d : v.getUserList()) {
            //只有store指令是局部变量的def
            if (d instanceof Store store) {
                defs.add(store.getBlock());
                if (!listW.contains(store.getBlock())) {
                    listW.add(store.getBlock());
                }
            }
        }

        while (!listW.isEmpty()) {
            BasicBlock blockX = listW.pop();
            if (dominAnalyzer.getDFOf(blockX) == null) {
                continue;
            }
            for (BasicBlock blockY : dominAnalyzer.getDFOf(blockX)) {
                if (!listF.contains(blockY)) {
                    blockY.addInstrAtEntry(new Phi(v, IRManager.getInstance().declareLocalVar()));
                    if (!listF.contains(blockY)) {
                        listF.add(blockY);
                    }
                    if (!defs.contains(blockY) && !listW.contains(blockY)) {
                        listW.add(blockY);
                    }
                }
            }
        }
    }

    private void rename(BasicBlock entry, HashMap<LocalDecl, Value> addrValue) {
        ArrayList<Instruction> instructions = new ArrayList<>(entry.getInstructionList());
        //更新到达定义
        for (Instruction instruction : instructions) {
            if (instruction instanceof LocalDecl localDecl && localDecl.getType() == ValueType.PI32) {
                addrValue.put(localDecl, new ConstNumber(0));
                entry.removeInstruction(instruction);
                instruction.destroy();
            } else if (instruction instanceof Store store &&
                    store.getAddr() instanceof LocalDecl addr && addr.getType() == ValueType.PI32) {
                addrValue.put(addr, store.getSrc());
                entry.removeInstruction(instruction);
                instruction.destroy();
            } else if (instruction instanceof Load load &&
                    load.getAddr() instanceof LocalDecl addr && addr.getType() == ValueType.PI32) {
                load.beReplacedBy(addrValue.get(addr));
                entry.removeInstruction(instruction);
                instruction.destroy();
            } else if (instruction instanceof Phi phi) {
                addrValue.put(phi.getPhiAddr(), phi);
            }
        }


        //更新CFG中后继块中的Phi
        HashSet<BasicBlock> children = Optimizer.instance().getCFG().getChildren(entry);
        if (children != null) {
            for (BasicBlock child : children) {
                for (Instruction instruction : child.getInstructionList()) {
                    if (instruction instanceof Phi phi) {
                        LocalDecl addr = phi.getPhiAddr();
                        //phi必须涵盖所有前驱块，即使其中没有定义点
                        if (addrValue.containsKey(addr) && addrValue.get(addr) != null) {
                            phi.addCond(addrValue.get(addr), entry);
                        } else {
                            phi.addCond(new ConstNumber(0), entry);
                        }
                    }
                }
            }
        }

        //在支配树中dfs
        children = dominAnalyzer.getDominTree().get(entry);
        if (children != null) {
            for (BasicBlock child : children) {
                rename(child, new HashMap<>(addrValue));
            }
        }
    }

    public void phiToMove() {
        for (Function function : module.getAllFunctions()) {
            ArrayList<BasicBlock> blocks = new ArrayList<>(function.getBlocks());
            for (BasicBlock block : blocks) {
                ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
                for (Instruction instruction : instructions) {
                    if (instruction instanceof Phi phi) {
                        phiToMoveForInstr(phi);
                        block.removeInstruction(phi);
                        //phi不能destroy，因为move实际上是对phi的value进行的赋值
                        //但是phi需要取消所有use，这会被move继承
                        for (Value operand : phi.getOperandList()) {
                            operand.removeUser(phi);
                        }
                    }
                }
            }
        }

        //之后，删除多余的move指令
        sortMove();
    }

    private void phiToMoveForInstr(Phi phi) {
        BasicBlock block = phi.getBlock();
        //获取其前驱块集合
        ArrayList<BasicBlock> prevBlocks = new ArrayList<>(Optimizer.instance().getCFG().getParents(block));
        for (BasicBlock prevBlock : prevBlocks) {
            int childrenNumber = Optimizer.instance().getCFG().getChildren(prevBlock).size();

            Phi phi_temp = phi.getPhiTmp();
            assert phi_temp != null;
            if (childrenNumber == 1) {
                //直接将move指令加在块尾
//                prevBlock.addInstructionBeforeMove(new MoveIR(phi_temp, phi.valueFromBlock(prevBlock)));
//                prevBlock.addInstructionBeforeBranch(new MoveIR(phi, phi_temp));
                prevBlock.addInstructionBeforeBranch(new MoveIR(phi, phi.valueFromBlock(prevBlock)));
            } else {
                //将move指令加在新建的中间块内
                //可能有多个phi指令选项来自同一个前驱，这时不能新建中间块而需要复用
//                BasicBlock midBlock = new BasicBlock();
//                midBlock.addInstructionAt(0, new MoveIR(phi_temp, phi.valueFromBlock(prevBlock)));
//                midBlock.addInstruction(new Br(block));
//                prevBlock.redirectTo(block, midBlock);
//                block.getFunction().addBlockBefore(block, midBlock);
//                midBlock.addInstructionBeforeBranch(new MoveIR(phi, phi_temp));

                BasicBlock midBlock = new BasicBlock();
                midBlock.addInstruction(new MoveIR(phi, phi.valueFromBlock(prevBlock)));
                midBlock.addInstruction(new Br(block));
                prevBlock.redirectTo(block, midBlock);
                block.getFunction().addBlockBefore(block, midBlock);

                //加入了中间块，那它一定被前驱支配，而不会支配后继
                midBlock.setLoopDepth(block.getLoopDepth());
                //加入了中间块，重新分析
                Optimizer.instance().getCFG().addMidBlock(prevBlock, block, midBlock);
                Optimizer.instance().getDominAnalyzer().addBlockBetween(prevBlock, block, midBlock);
            }
        }
    }

    private void sortMove() {
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock block : f.getBlocks()) {
                LinkedList<MoveIR> moves = new LinkedList<>();
                LinkedList<Value> src = new LinkedList<>();

                int moveBeginIndex = -1;

                for (int i = 0; i < block.getInstructionList().size(); i++) {
                    if (block.getInstructionList().get(i) instanceof MoveIR moveIR) {
                        if (moveBeginIndex == -1) {
                            moveBeginIndex = i;
                        }
                        moves.add(moveIR);
                        src.add(moveIR.getOperandList().get(0));
                    }
                }

                if (moveBeginIndex == -1) {
                    continue;
                }

                ArrayList<MoveIR> newMoveIRs = new ArrayList<>();

                while (!moves.isEmpty()) {
                    MoveIR moveIR = moves.poll();
                    if (moveIR.getSrc().equals(moveIR.getOriginPhi())) {
                        continue;
                    }

                    if (src.contains(moveIR.getOriginPhi())) {
                        moves.add(moveIR);
                    } else {
                        src.poll();
                        newMoveIRs.add(moveIR);
                    }
                }

                block.getInstructionList().removeAll(newMoveIRs);
                for (int i = newMoveIRs.size() - 1; i >= 0; i--) {
                    block.addInstructionAt(moveBeginIndex, newMoveIRs.get(i));
                }
            }
        }
    }
}