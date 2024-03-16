package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Call;
import IntermediatePresentation.Instruction.CmpThenBr;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.MoveIR;
import IntermediatePresentation.Instruction.Phi;
import IntermediatePresentation.Instruction.Putstr;
import IntermediatePresentation.Instruction.Ret;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Module;
import IntermediatePresentation.User;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;

import java.util.ArrayList;
import java.util.HashSet;

public class DeadCode {
    private final Module module;
    private ControlFlowGraph CFG;

    public DeadCode(Module module) {
        this.module = module;
    }

    public void optimize(ControlFlowGraph CFG) {
        this.CFG = CFG;
        //去除掉流图中没有父母的不可达块
        removeUnreachableBlock();
        removeUnusedInstructions();
        removeUnusedInstruction();
    }

    public void optimize(ControlFlowGraph CFG, Function f) {
        this.CFG = CFG;
        removeUnreachableBlockForFunction(f);
        for (BasicBlock b : f.getBlocks()) {
            removeUnusedInstructionForBlock(b);
        }
    }

    public void scanJump() {
        //删除基本块跳转指令之后的指令，暂不考虑全局关系
        for (Function function : module.getFunctions()) {
            for (BasicBlock bb : function.getBlocks()) {
                scanJumpForBlock(bb);
            }
        }
        for (BasicBlock bb : module.getMainFunction().getBlocks()) {
            scanJumpForBlock(bb);
        }
    }

    private void scanJumpForBlock(BasicBlock block) {
        boolean reachEnd = false;
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (Instruction instruction : block.getInstructionList()) {
            if (!reachEnd) {
                instructions.add(instruction);
                if (instruction instanceof Ret || instruction instanceof Br) {
                    reachEnd = true;
                }
            } else {
                instruction.destroy();
            }
        }
        block.updataInstructionList(instructions);
    }

    private void removeUnreachableBlock() {
        for (Function function : module.getFunctions()) {
            removeUnreachableBlockForFunction(function);
        }
        removeUnreachableBlockForFunction(module.getMainFunction());
    }

    private void removeUnreachableBlockForFunction(Function function) {
        HashSet<BasicBlock> blocks = new HashSet<>(function.getBlocks());
        boolean hasChanged;
        do {
            HashSet<BasicBlock> reachedBlocks = CFG.reachedBlocks(function);
            hasChanged = !blocks.equals(reachedBlocks);
            if (hasChanged) {
                function.getBlocks().retainAll(reachedBlocks);
                blocks = new HashSet<>(function.getBlocks());
            }
        } while (hasChanged);
    }

    private void removeUnusedInstruction() {
        for (Function function : module.getAllFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                removeUnusedInstructionForBlock(block);
            }
        }
    }

    private void removeUnusedInstructionForBlock(BasicBlock block) {
        ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructionList());
        boolean hasChanged;
        do {
            hasChanged = false;
            for (Instruction instruction : instructions) {
                if (instruction.isUseless()) {
                    hasChanged = true;
                    block.removeInstruction(instruction);
                    //调用destroy方法将其在def-use链中去掉
                    instruction.destroy();
                }
            }
            instructions = new ArrayList<>(block.getInstructionList());
        } while (hasChanged);
    }

    private void removeUnusedInstructions() {
        //首先求出一个有用指令的闭包
        //"有用"的指令包括：store ret br call putstr
        HashSet<Instruction> usefulInstrs = new HashSet<>();
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock b : f.getBlocks()) {
                for (Instruction i : b.getInstructionList()) {
                    if (i instanceof Ret || i instanceof Br || i instanceof Putstr || i instanceof CmpThenBr) {
                        usefulInstrs.add(i);
                    } else if (i instanceof Call call &&
                            Optimizer.instance().hasSideEffect(call.getCallingFunction())) {
                        usefulInstrs.add(i);
                    } else if (i instanceof Store) {
                        usefulInstrs.add(i);
                    }
                }
            }
        }

        //根据闭包扩大有用指令集并删除无用指令
        boolean hasChanged;
        do {
            hasChanged = false;
            HashSet<Instruction> userfulInstrsCopy = new HashSet<>(usefulInstrs);
            for (Instruction i : userfulInstrsCopy) {
                for (Value operand : i.getOperandList()) {
                    if (operand instanceof Instruction opIns && !usefulInstrs.contains(operand)) {
                        usefulInstrs.add(opIns);
                        hasChanged = true;
                    }
                    if (operand instanceof Phi phi) {
                        for (MoveIR moveIR : phi.getMoveIrs()) {
                            if (!usefulInstrs.contains(moveIR)) {
                                usefulInstrs.add(moveIR);
                                hasChanged = true;
                            }
                        }
                    }
                }

                if (i.getType().equals(ValueType.PI32) || i.getType().equals(ValueType.ARRAY)) {
                    //如果这个地址会被使用，那么也要包含所有对这个地址的store
                    for (User user : i.getUserList()) {
                        if (user instanceof Store store && !usefulInstrs.contains(store)) {
                            usefulInstrs.add(store);
                            hasChanged = true;
                        }
                    }
                    if (i instanceof GetElementPtr gep) {
                        Value ptr = gep.getPtr();
                        for (User user : gep.getPtr().getUserList()) {
                            //如果地址是gep得到的，那也要算上其ptr
                            if (user instanceof Instruction instruction && !usefulInstrs.contains(user)) {
                                usefulInstrs.add(instruction);
                                hasChanged = true;
                            }
                        }
                    }
                }
            }
        } while (hasChanged);


        //删除无用指令
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock b : f.getBlocks()) {
                ArrayList<Instruction> instructions = new ArrayList<>(b.getInstructionList());
                for (Instruction instruction : instructions) {
                    if (!usefulInstrs.contains(instruction)) {
                        b.removeInstruction(instruction);
                        instruction.destroy();
                    }
                }
            }
        }
    }
}
