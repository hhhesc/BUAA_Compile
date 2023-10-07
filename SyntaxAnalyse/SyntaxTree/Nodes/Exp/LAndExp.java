package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

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
}
