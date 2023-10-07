package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class MainFuncDef extends SyntaxTreeNode {
    public MainFuncDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.MainFuncDef, parent);
    }
}
