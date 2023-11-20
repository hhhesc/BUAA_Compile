package TargetCode;

import IntermediatePresentation.BasicBlock;
import TargetCode.GlobalData.DataDecl;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Instruction.MipsInstr;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsFile {
    private final ArrayList<DataDecl> datas = new ArrayList<>();
    private final ArrayList<MipsStmt> mipsStmts = new ArrayList<>();

    public void addData(DataDecl data) {
        datas.add(data);
    }

    public void addInstr(MipsInstr instr) {
        mipsStmts.add(instr);
    }

    public void tagBBWithLabel( Label label) {
        mipsStmts.add(label);
    }

    public void insertLabel(Label label) {
        mipsStmts.add(label);
    }

    public void insertAnnotation(String annotation) {
        mipsStmts.add(new Annotation(annotation));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (DataDecl data : datas) {
            sb.append(data);
        }
        sb.append("\n.text\n");
        sb.append("jal main\n");
        sb.append("j end\n");

        for (MipsStmt stmt : mipsStmts) {
            sb.append(stmt.toString());
        }
        sb.append("\n");
        return sb.toString();
    }
}
