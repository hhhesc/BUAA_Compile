package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class IntConst extends SyntaxTreeNode {
    public IntConst(SyntaxTreeNode parent) {
        super(SyntaxNodeType.IntConst, parent);
    }

}
