package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Number extends SyntaxTreeNode {
    public Number(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Number, parent);
    }
}
