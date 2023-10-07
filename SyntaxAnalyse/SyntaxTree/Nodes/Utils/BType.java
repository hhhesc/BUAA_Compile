package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class BType extends SyntaxTreeNode {
    public BType(SyntaxTreeNode parent) {
        super(SyntaxNodeType.BType, parent);
    }
}
