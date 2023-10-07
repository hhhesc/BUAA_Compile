package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class InitVal extends SyntaxTreeNode {
    public InitVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.InitVal, parent);
    }
}
