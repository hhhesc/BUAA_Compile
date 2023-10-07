package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncType extends SyntaxTreeNode {
    public FuncType(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncType, parent);
    }
}
