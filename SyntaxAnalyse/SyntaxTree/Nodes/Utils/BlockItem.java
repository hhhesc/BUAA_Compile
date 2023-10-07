package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class BlockItem extends SyntaxTreeNode {
    public BlockItem(SyntaxTreeNode parent) {
        super(SyntaxNodeType.BlockItem, parent);
    }
}
