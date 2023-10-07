package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class LVal extends SyntaxTreeNode {
    public LVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.LVal, parent);
    }
}
