package Optimizer;

import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jr;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.OffsetRegAddr;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.Instruction.MipsInstr;
import TargetCode.MipsBlock;
import TargetCode.MipsFile;
import TargetCode.MipsStmt;
import TargetCode.Register;
import TargetCode.RegisterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InstructionMerge {
    private final MipsFile mipsFile;

    public InstructionMerge() {
        this.mipsFile = Optimizer.instance().getMipsFile();
    }

    public void optimize() {
        repeatedStore();
    }

    private void repeatedStore() {
        removeUnusedLw();
        removeUnusedSw();

    }

    private void removeUnusedSw() {

        for (MipsBlock block : mipsFile.getMipsBlockList()) {
            HashMap<OffsetRegAddr, Sw> lastSw = new HashMap<>();
            //lw、sw、move都是生成Mips过程中使用的转移指令，因此可能会产生冗余
            for (MipsStmt stmt : new ArrayList<>(block.getStmts())) {
                if (stmt instanceof Sw sw) {
                    //直到lw之前，除了最后一个，所有的sw都可以去掉
                    if (lastSw.containsKey(sw.getOffsetRegAddr())) {
                        block.removeStmt(lastSw.get(sw.getOffsetRegAddr()));
                    }
                    lastSw.put(sw.getOffsetRegAddr(), sw);
                } else if (stmt instanceof Lw lw) {
                    //如果有lw出现，那之前的sw就不是无用的，就不需要去除
                    lastSw.remove(lw.getOffsetRegAddr());
                }

                if (stmt instanceof MipsInstr mipsInstr) {
                    //如果进行了跳转，那就不能确定当前的数据是否准确
                    //由于block是从llvm中的bb的来的，因此这里的跳转实际就是函数调用jal
                    if (mipsInstr.isBranchInstr()) {
                        //只有sp可以保证是准确的
//                        HashSet<OffsetRegAddr> keys = new HashSet<>(lastSw.keySet());
//                        for (OffsetRegAddr addr : keys) {
//                            if (!addr.getReg().equals(RegisterManager.sp)) {
//                                lastSw.remove(addr);
//                            }
//                        }
                        lastSw.clear();
                    } else if (mipsInstr.putToRegister() != null &&
                            !(mipsInstr.putToRegister().equals(RegisterManager.sp))) {
                        //如果破坏了，那之前的地址就无效了，除非是sp，它只有在函数调用才会改变且会立即恢复
                        Register destoryReg = mipsInstr.putToRegister();
                        HashSet<OffsetRegAddr> keys = new HashSet<>(lastSw.keySet());
                        for (OffsetRegAddr addr : keys) {
                            if (addr.getReg().equals(destoryReg)) {
                                lastSw.remove(addr);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
               直到sw之前，如果lw得到的寄存器都没有被使用，那么可以将其删去
               这个主要是为了在多个call之间减少寄存器的存取，所以出于正确性考虑，只判断$sp的即可
    */
    private void removeUnusedLw() {
        for (MipsBlock block : mipsFile.getMipsBlockList()) {
            //lw、sw、move都是生成Mips过程中使用的转移指令，因此可能会产生冗余
            HashMap<Integer, Lw> unUsedStackLoad = new HashMap<>();
            for (MipsStmt stmt : new ArrayList<>(block.getStmts())) {
                //如果load出的寄存器被破坏了，则立即将lw删除；如果被使用了，则将其从待删除指令中去掉
                if (stmt instanceof MipsInstr instr) {
                    Register destoryReg = instr.putToRegister();
                    ArrayList<Register> usedRegs = instr.operandRegs();
                    HashSet<Integer> stackPointers = new HashSet<>(unUsedStackLoad.keySet());

                    for (Integer sp : stackPointers) {
                        Register lwDest = unUsedStackLoad.get(sp).getDest();

                        if (usedRegs.contains(lwDest)) {
                            unUsedStackLoad.remove(sp);
                        } else if (lwDest.equals(destoryReg)) {
                            block.removeStmt(unUsedStackLoad.get(sp));
                            unUsedStackLoad.remove(sp);
                        }
                    }

                    if (stmt instanceof Branch || stmt instanceof J || stmt instanceof Jr) {
                        //如果遇到了跳转指令，则清空
                        //不考虑jal是因为它并不会改变栈内容
                        unUsedStackLoad.clear();
                    }
                }

                //要在判断lw之前进行load出的寄存器是否需要使用的判断
                if (stmt instanceof Lw lw && lw.getOffsetRegAddr().getReg().equals(RegisterManager.sp)) {
                    unUsedStackLoad.put(lw.getOffsetRegAddr().getOffset(), lw);
                }
            }
        }
    }
}
