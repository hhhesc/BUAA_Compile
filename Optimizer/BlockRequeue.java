package Optimizer;

import TargetCode.MipsBlock;
import TargetCode.MipsFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class BlockRequeue {
    private final MipsFile mipsFile;

    public BlockRequeue() {
        this.mipsFile = Optimizer.instance().getMipsFile();
    }

    public void optimize() {
        LinkedList<MipsBlock> originBlockList = new LinkedList<>(mipsFile.getMipsBlockList());

        ArrayList<MipsBlock> newBlockList = new ArrayList<>();
        HashSet<MipsBlock> visited = new HashSet<>();
        MipsBlock block = originBlockList.poll();
        newBlockList.add(block);
        visited.add(block);

        while (!originBlockList.isEmpty()) {
            MipsBlock tar;
            if (block != null && block.jumpTo() != null && !visited.contains(block.jumpTo())) {
                tar = block.jumpTo();
            } else {
                tar = originBlockList.poll();
            }

            if (!newBlockList.contains(tar)) {
                newBlockList.add(tar);
                originBlockList.remove(tar);
            }
            block = tar;
            visited.add(tar);
        }
        mipsFile.requeue(newBlockList);
    }
}
