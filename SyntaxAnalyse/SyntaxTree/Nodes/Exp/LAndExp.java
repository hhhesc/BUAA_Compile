package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.ZextTo;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.NodeBuilder;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class LAndExp extends SyntaxTreeNode {
    public LAndExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LAndExp, parent);
    }

    public void adjust() {
        int size = children.size();
        if (size > 1) {
            SyntaxTreeNode child = NodeBuilder.buildIntermediateNode(type, this);
            for (int i = 0; i < size - 2; i++) {
                child.addChild(children.get(i));
            }
            ArrayList<SyntaxTreeNode> adjustedChildren = new ArrayList<>();
            child.adjust();
            adjustedChildren.add(child);
            adjustedChildren.add(children.get(size - 2));
            adjustedChildren.add(children.get(size - 1));
            children = adjustedChildren;
        }
    }

    public void condToIR(BasicBlock ifTrue, BasicBlock ifFalse) {
        Value cond;

        if (children.size() == 3) {
            BasicBlock curBB = IRManager.getInstance().getCurBlock();
            BasicBlock trueThen = new BasicBlock();
            IRManager.getInstance().setCurBlock(curBB);
            ((LAndExp) children.get(0)).condToIR(trueThen, ifFalse);
            //继续判断
            IRManager.getInstance().setCurBlock(trueThen);
            cond = children.get(2).toIR();
        } else {
            cond = children.get(0).toIR();
        }
        //前面都是真，表达式的值取决于这一个式子
        if (cond.isPointer()) {
            cond = new Load(IRManager.getInstance().declareTempVar(), cond);
        }
        if (cond.getType() != ValueType.I1) {
            cond = new Icmp("!=", cond, new ConstNumber(0, cond.getType()));
        }
        new Br(cond, ifTrue, ifFalse);
    }
}
