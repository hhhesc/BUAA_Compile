package Optimizer;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.Function;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.CmpThenBr;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Instruction;
import IntermediatePresentation.Module;

import java.util.ArrayList;

public class ToCmpThenBr {
    private final Module module;

    public ToCmpThenBr() {
        this.module = Optimizer.instance().getModule();
    }

    public void optimize() {
        for (Function f : module.getAllFunctions()) {
            for (BasicBlock b : f.getBlocks()) {
                ArrayList<Instruction> instructions = new ArrayList<>(b.getInstructionList());
                for (Instruction i : instructions) {
                    if (i instanceof Icmp icmp && icmp.getUserList().size() == 1 &&
                            icmp.getUserList().get(0) instanceof Br br) {
                        //如果被ZextTo使用了，那么就不能合并，所以一定是size==1时
                        CmpThenBr cmb = new CmpThenBr(icmp.getCond(), icmp.getOperandList().get(0),
                                icmp.getOperandList().get(1), br.getIfTrue(), br.getIfFalse());

                        icmp.getBlock().removeInstruction(icmp);
                        icmp.destroy();
                        int idx = br.getBlock().getInstructionList().indexOf(br);
                        br.getBlock().removeInstruction(br);
                        br.destroy();
                        br.getBlock().addInstructionAt(idx, cmb);
                    }
                }
            }
        }
    }
}
