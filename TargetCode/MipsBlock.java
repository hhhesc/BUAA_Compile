package TargetCode;


import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jr;
import TargetCode.Instruction.MipsInstr;

import java.util.ArrayList;

public class MipsBlock {
    private final Label bbLabel;
    private final ArrayList<MipsStmt> mipsStmts = new ArrayList<>();

    public MipsBlock(Label bbLabel) {
        this.bbLabel = bbLabel;
        mipsStmts.add(bbLabel);
    }

    public void addInstr(MipsInstr instr) {
        mipsStmts.add(instr);
        instr.setBlock(this);
    }

    public void insertLabel(Label label) {
        mipsStmts.add(label);
        label.setBlock(this);
    }

    public void insertAnnotation(String annotation) {
        mipsStmts.add(new Annotation(annotation));
    }

    public void removeStmt(MipsStmt mipsStmt) {
        mipsStmts.remove(mipsStmt);
    }

    public void replaceStmtWith(MipsStmt src, MipsStmt newSrc) {
        int idx = mipsStmts.indexOf(src);
        mipsStmts.set(idx, newSrc);
        newSrc.setBlock(this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (MipsStmt stmt : mipsStmts) {
            sb.append(stmt.toString());
        }
        sb.append("\n");
        return sb.toString();
    }

    public ArrayList<MipsStmt> getStmts() {
        return mipsStmts;
    }

    public MipsBlock jumpTo() {
        MipsBlock tar = MipsManager.getFile().getNext(this);
        for (MipsStmt stmt : mipsStmts) {
            if (stmt instanceof J j) {
                tar = MipsManager.getFile().getBlock(j.getLabel().toString());
                break;
            } else if (stmt instanceof Jr) {
                return null;
            }
        }
        return tar;
    }
}
