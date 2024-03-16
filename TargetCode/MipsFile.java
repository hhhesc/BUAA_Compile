package TargetCode;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import TargetCode.GlobalData.Asciiz;
import TargetCode.GlobalData.DataDecl;
import TargetCode.Instruction.Jump.Branch;
import TargetCode.Instruction.Jump.J;
import TargetCode.Instruction.Jump.Jal;
import TargetCode.Instruction.La;
import TargetCode.Instruction.Li;
import TargetCode.Instruction.MipsInstr;
import TargetCode.Instruction.Syscall;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsFile {
    private MipsBlock curBlock;
    private final ArrayList<DataDecl> datas = new ArrayList<>();
    private final HashMap<String, MipsBlock> mipsBlockMap = new HashMap<>();
    private ArrayList<MipsBlock> mipsBlockList = new ArrayList<>();

    public void addData(DataDecl data) {
        datas.add(data);
    }

    public void addInstr(MipsInstr instr) {
        curBlock.addInstr(instr);
    }

    public void tagBBWithLabel(Label label) {
        curBlock = new MipsBlock(label);
        mipsBlockList.add(curBlock);
        mipsBlockMap.put(label.toString(), curBlock);
    }

    public void insertLabel(Label label) {
        curBlock = new MipsBlock(label);
        mipsBlockList.add(curBlock);
        mipsBlockMap.put(label.toString(), curBlock);
    }

    public void insertAnnotation(String annotation) {
        curBlock.insertAnnotation(annotation);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (DataDecl data : datas) {
            sb.append(data);
        }
        sb.append("\n.text\n");

//        sb.append("jal main\n");
//        for (MipsBlock block : mipsBlockList) {
//            sb.append(block.toString());
//        }

        int mainIdx = mipsBlockList.indexOf(mipsBlockMap.get("main:\n"));
        for (int i = mainIdx; i < mipsBlockList.size(); i++) {
            sb.append(mipsBlockList.get(i).toString());
        }
        for (int i = 0; i < mainIdx; i++) {
            sb.append(mipsBlockList.get(i).toString());
        }

        sb.append("\n");
        return sb.toString();
    }

    public ArrayList<MipsStmt> getMipsStmts() {
        ArrayList<MipsStmt> stmts = new ArrayList<>();
        for (MipsBlock block : mipsBlockList) {
            for (MipsStmt stmt : block.getStmts()) {
                if (!(stmt instanceof Annotation)) {
                    stmts.add(stmt);
                }
            }
        }
        return stmts;
    }

    public void removeStmt(MipsStmt mipsStmt) {
        mipsStmt.getBlock().removeStmt(mipsStmt);
    }

    public void replaceStmtWith(MipsStmt src, MipsStmt newSrc) {
        src.getBlock().replaceStmtWith(src, newSrc);
    }

    public MipsBlock getBlock(String label) {
        return mipsBlockMap.get(label);
    }

    public MipsBlock getNext(MipsBlock block) {
        if (mipsBlockList.contains(block) && mipsBlockList.indexOf(block) + 1 < mipsBlockList.size()) {
            return mipsBlockList.get(mipsBlockList.indexOf(block) + 1);
        } else {
            return null;
        }
    }

    public ArrayList<MipsBlock> getMipsBlockList() {
        return new ArrayList<>(mipsBlockList);
    }

    public void requeue(ArrayList<MipsBlock> newBlockList) {
        //可以对没有branch跳转的块进行合并，使得后面merge和peephole的机会更多
        this.mipsBlockList = newBlockList;
    }
}
