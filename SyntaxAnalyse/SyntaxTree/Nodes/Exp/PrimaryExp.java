package SyntaxAnalyse.SyntaxTree.Nodes.Exp;

import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.LVal;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Number;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class PrimaryExp extends SyntaxTreeNode {
    public PrimaryExp(SyntaxTreeNode parent) {
        super(SyntaxNodeType.PrimaryExp, parent);
    }

    public Integer getDim() {
        for (SyntaxTreeNode child : children) {
            if (child.getDim() != null) {
                return child.getDim();
            }
        }
        return null;
    }

    public Integer getVal() {
        if (children.get(0) instanceof Number) {
            return Integer.parseInt(getFirstLeafString());
        } else if (children.get(0) instanceof LVal) {
            return ((LVal) children.get(0)).getVal();
        } else {
            return ((Exp) children.get(1)).getVal();
        }
    }

    public Value toIR() {
        if (children.size() == 1) {
            //Number or Lval
            return children.get(0).toIR();
        } else {
            //'(' Exp ')'
            return children.get(1).toIR();
        }
    }

}
