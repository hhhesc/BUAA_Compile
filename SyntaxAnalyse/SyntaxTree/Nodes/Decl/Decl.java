package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Decl extends SyntaxTreeNode {
    public Decl(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Decl, parent);
    }

    public Value toIR() {
        return children.get(0).toIR();
    }
}
