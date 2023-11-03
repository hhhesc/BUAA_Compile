package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.Exp;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class InitVal extends SyntaxTreeNode {
    public InitVal(SyntaxTreeNode parent) {
        super(SyntaxNodeType.InitVal, parent);
    }

    public int getVal() {
        return ((Exp) children.get(0)).getVal();
    }

    public Value toIR() {
        if (children.size() == 1) {
            return children.get(0).toIR();
        } else {
            ArrayInitializer initializer = new ArrayInitializer(new ArrayList<>());
            for (SyntaxTreeNode child : children) {
                if (child instanceof InitVal) {
                    Value v = child.toIR();
                    if (v instanceof ArrayInitializer aiv) {
                        initializer.merge(aiv);
                    } else {
                        initializer.add(v);
                    }
                }
            }
            return initializer;
        }
    }
}
