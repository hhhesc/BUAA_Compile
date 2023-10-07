package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.FuncDefParser;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncDef extends SyntaxTreeNode {
    public FuncDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncDef, parent);
    }

}
