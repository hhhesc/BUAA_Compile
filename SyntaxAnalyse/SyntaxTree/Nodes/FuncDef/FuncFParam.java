package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncFParam extends SyntaxTreeNode {
    public FuncFParam(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncFParam, parent);
    }
}
