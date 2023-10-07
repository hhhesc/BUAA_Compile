package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class ConstDef extends SyntaxTreeNode {
    public ConstDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstDef, parent);
    }
}
