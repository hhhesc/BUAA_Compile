package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class FormatString extends SyntaxTreeNode {
    public FormatString(SyntaxTreeNode parent){
        super(SyntaxNodeType.FormatString,parent);
    }
}
