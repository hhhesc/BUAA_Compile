package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class VarDef extends SyntaxTreeNode {
    public VarDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.VarDef, parent);
    }
}
