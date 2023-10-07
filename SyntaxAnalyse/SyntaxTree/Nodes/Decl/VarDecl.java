package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class VarDecl extends SyntaxTreeNode {
    public VarDecl(SyntaxTreeNode parent) {
        super(SyntaxNodeType.VarDecl, parent);
    }
}
