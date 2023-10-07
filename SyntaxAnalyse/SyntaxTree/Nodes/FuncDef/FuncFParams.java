package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncFParams extends SyntaxTreeNode {
    public FuncFParams(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncFParams, parent);
    }
}
