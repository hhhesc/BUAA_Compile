package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Block extends SyntaxTreeNode {
    public Block(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Block, parent);
    }
}
