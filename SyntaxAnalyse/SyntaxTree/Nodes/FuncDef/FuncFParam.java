package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;


import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.ConstExp;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Ident;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;
import java.util.Objects;

public class FuncFParam extends SyntaxTreeNode {
    public FuncFParam(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncFParam, parent);
    }

    public Integer getDim() {
        if (children.size() == 2) {
            return 0;
        } else if (children.size() == 4) {
            return 1;
        } else {
            return 2;
        }
    }

    public String getIdent() {
        for (SyntaxTreeNode child : children) {
            if (child instanceof Ident) {
                return ((Ident) child).getIdent().getSrcStr();
            }
        }
        return null;
    }

    public Value toIR() {
        String ident = getIdent();
        Value fPara, paraValue = null;
        if (children.size() == 2) {
            fPara = new Value(IRManager.getInstance().declareParam(), ValueType.I32);
            LocalDecl p = new LocalDecl();
            new Store(fPara, p);
            symbolTableManager.varDecl(ident, false, 0, new ArrayList<>());
            paraValue = p;
        } else if (children.size() == 4) {
            fPara = new Value(IRManager.getInstance().declareParam(), ValueType.PI32);
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(1000);
            symbolTableManager.varDecl(ident, false, 1, lens);
        } else {
            fPara = new Value(IRManager.getInstance().declareParam(), ValueType.PI32);
            int len = 0;
            for (SyntaxTreeNode child : children) {
                if (child instanceof ConstExp) {
                    len = ((ConstExp) child).getVal();
                    break;
                }
            }
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(1000);
            lens.add(len);
            symbolTableManager.varDecl(ident, false, 2, lens);
        }
        symbolTableManager.setIRValue(ident, Objects.requireNonNullElse(paraValue, fPara));
        return fPara;
    }
}
