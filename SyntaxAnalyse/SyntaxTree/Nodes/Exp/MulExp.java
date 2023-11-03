package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.ALU;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.NodeBuilder;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class MulExp extends SyntaxTreeNode {
    private String op;

    public MulExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.MulExp, parent);
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

    public Integer getDim() {
        for (SyntaxTreeNode child : children) {
            if (child.getDim() != null) return child.getDim();
        }
        return null;
    }

    public Integer getVal() {
        if (children.size() == 1) {
            return ((UnaryExp) children.get(0)).getVal();
        } else {
            op = children.get(1).getFirstLeafString();
            int oprand1 = ((MulExp) children.get(0)).getVal();
            int oprand2 = ((UnaryExp) children.get(2)).getVal();
            if (op.equals("*")) {
                return oprand1 * oprand2;
            } else if (op.equals("/")) {
                return oprand1 / oprand2;
            } else {
                return oprand1 % oprand2;
            }
        }
    }

    public Value toIR() {
        if (children.size() == 1) {
            return children.get(0).toIR();
        } else {
            return new ALU(children.get(0).toIR(), children.get(1).getFirstLeafString()
                    , children.get(2).toIR());
        }
    }
}
