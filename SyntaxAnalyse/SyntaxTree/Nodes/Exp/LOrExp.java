package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Br;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.NodeBuilder;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class LOrExp extends SyntaxTreeNode {
    public LOrExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LOrExp, parent);
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
        if (children.size() == 3) {
            BasicBlock curBB = IRManager.getInstance().getCurBlock();
            BasicBlock falseThen = new BasicBlock();

            IRManager.getInstance().setCurBlock(curBB);
            ((LOrExp) children.get(0)).condToIR(ifTrue, falseThen);

            IRManager.getInstance().setCurBlock(falseThen);
            ((LAndExp) children.get(2)).condToIR(ifTrue,ifFalse);
            //继续判断，为真直接跳过去
        } else {
            //前面都是假，表达式的值取决于这一个式子
            ((LAndExp) children.get(0)).condToIR(ifTrue, ifFalse);
        }
    }
}
