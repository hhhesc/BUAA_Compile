package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ConstInitVal extends SyntaxTreeNode {
    public ConstInitVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstInitVal, parent);
    }
}
