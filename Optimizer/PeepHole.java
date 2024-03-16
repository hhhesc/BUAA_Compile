package Optimizer;

import TargetCode.Instruction.ALU.Addi;
import TargetCode.Instruction.ALU.Scmp;
import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Memory.Lw;
import TargetCode.Instruction.Memory.Sw;
import TargetCode.Instruction.MipsInstr;
import TargetCode.Instruction.Move;
import TargetCode.Label;
import TargetCode.MipsFile;
import TargetCode.MipsStmt;

import java.util.ArrayList;

public class PeepHole {
    private MipsFile mipsFile;


    public void optimizeMips() {
        this.mipsFile = Optimizer.instance().getMipsFile();
        normalOptimize();
    }

    public void normalOptimize() {
        ArrayList<MipsStmt> mipsStmts = mipsFile.getMipsStmts();
        for (int i = 0; i < mipsStmts.size(); i++) {
            MipsStmt stmt = mipsStmts.get(i);
            if (stmt instanceof MipsInstr) {
                // addi $r,$r,0
                if (stmt instanceof Addi addi && addi.getImm() == 0 && addi.getDest().equals(addi.getSrc())) {
                    mipsFile.removeStmt(addi);
                }

                //move $r,$r
                if (stmt instanceof Move move && move.getDst().equals(move.getSrc())) {
                    mipsFile.removeStmt(move);
                }

                //move $r0,$r1 ; move $r0,$r2
                //这里考虑的是move指令的覆盖，所以后一个指令不能是move $r0,$r0
                if (stmt instanceof Move move1 && i < mipsStmts.size() - 1 && mipsStmts.get(i + 1) instanceof Move move2
                        && move1.getDst().equals(move2.getDst()) && !move2.getDst().equals(move2.getSrc())) {
                    mipsFile.removeStmt(move1);
                }

                // j b0 ; b0:
                if (stmt instanceof J j && i < mipsStmts.size() - 1 && mipsStmts.get(i + 1) instanceof Label label
                        && j.getLabel().equals(label)) {
                    mipsFile.removeStmt(j);
                }

                //sw $r0,addr($sp) ; lw $r1,addr($sp) ; ...
                if (stmt instanceof Sw sw && i < mipsStmts.size() - 1) {
                    if (i + 1 < mipsStmts.size()) {
                        if (mipsStmts.get(i + 1) instanceof Lw lw) {
                            if (sw.getOffsetRegAddr().equals(lw.getOffsetRegAddr())) {
                                mipsFile.replaceStmtWith(lw, new Move(lw.getDest(), sw.getSrc()));
                            }
                        }
                    }
                }
            }
        }
    }
}
