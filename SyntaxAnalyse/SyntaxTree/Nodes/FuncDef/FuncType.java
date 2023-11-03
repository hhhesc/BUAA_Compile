package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FuncType extends SyntaxTreeNode {
    public FuncType(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncType, parent);
    }

    public boolean isVoid(){
        return children.get(0).getWord().getSrcStr().equals("void");
    }
}
