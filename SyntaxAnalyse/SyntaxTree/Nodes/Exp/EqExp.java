package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Icmp;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.NodeBuilder;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class EqExp extends SyntaxTreeNode {
    public EqExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.EqExp, parent);
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

    public Value toIR() {
        if (children.size() == 1) {
            return children.get(0).toIR();
        } else {
            return new Icmp(children.get(1).getFirstLeafString(), children.get(0).toIR(),
                    children.get(2).toIR());
        }
    }
}
