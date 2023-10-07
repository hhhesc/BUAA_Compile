package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncRParams extends SyntaxTreeNode {
    public FuncRParams(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncRParams, parent);
    }
}
