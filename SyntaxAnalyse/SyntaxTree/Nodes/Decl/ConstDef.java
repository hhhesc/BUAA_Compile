package SyntaxAnalyse.SyntaxTree.Nodes.Decl;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.Array.ArrayInitializer;
import IntermediatePresentation.ConstNumber;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.GetElementPtr;
import IntermediatePresentation.Instruction.GlobalDecl;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import LexicalAnalyse.Words.Word;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.ConstExp;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Ident;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

// ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
public class ConstDef extends SyntaxTreeNode {
    private Word ident;
    private int dim;

    public ConstDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.ConstDef, parent);
    }

    public void checkError() {
        int firstDim = 0;
        int secondDim = 0;
        ConstInitVal initVal = null;

        for (SyntaxTreeNode child : children) {
            if (child instanceof ConstExp && firstDim == 0) {
                firstDim = ((ConstExp) child).getVal();
            } else if (child instanceof ConstExp && secondDim == 0) {
                secondDim = ((ConstExp) child).getVal();
            } else if (child instanceof ConstInitVal) {
                initVal = ((ConstInitVal) child);
            }
        }
        assert initVal != null;

        ident = ((Ident) children.get(0)).getIdent();
        ArrayList<Integer> lens = new ArrayList<>();
        int cnt = 0;
        for (SyntaxTreeNode child : children) {
            if (child.isLeaf() && child.getWord().getSrcStr().equals("[")) {
                cnt++;
            } else if (child instanceof ConstExp) {
                lens.add(((ConstExp) child).getVal());
            }
        }
        dim = cnt;

        if (symbolTableManager.notDeclaredInCurLevel(ident.getSrcStr())) {
            symbolTableManager.varDecl(ident.getSrcStr(), true, dim, lens);
            symbolTableManager.setVal(ident.getSrcStr(), initVal.getVal());
        } else {
            ErrorManager.addError('b', ident.getLineNumber());
        }

        super.checkError();
    }

    public Value toIR() {
        String ident = null;
        int firstDim = 0;
        int secondDim = 0;
        ConstInitVal initVal = null;

        for (SyntaxTreeNode child : children) {
            if (child instanceof Ident) {
                ident = child.getFirstLeafString();
            } else if (child instanceof ConstExp && firstDim == 0) {
                firstDim = ((ConstExp) child).getVal();
            } else if (child instanceof ConstExp && secondDim == 0) {
                secondDim = ((ConstExp) child).getVal();
            } else if (child instanceof ConstInitVal) {
                initVal = ((ConstInitVal) child);
            }
        }
        assert initVal != null;

        if (children.size() == 3) {
            try {
                int val = initVal.getVal();

                symbolTableManager.varDecl(ident, true,
                        0, new ArrayList<>());
                symbolTableManager.setVal(ident, val);

                //可以求出常数形式的初始值
                if (IRManager.getInstance().inGlobalDecl()) {
                    GlobalDecl globalDecl = new GlobalDecl(new ConstNumber(Integer.toString(val)));

                    symbolTableManager.setIRValue(ident, globalDecl);
                } else {
                    LocalDecl localDecl = new LocalDecl();
                    new Store(new ConstNumber(val), localDecl);

                    symbolTableManager.setIRValue(ident, localDecl);
                }
            } catch (NullPointerException e) {
                symbolTableManager.varDecl(ident, true,
                        0, new ArrayList<>());
                if (IRManager.getInstance().inGlobalDecl()) {
                    GlobalDecl globalDecl = new GlobalDecl(children.get(2).toIR());

                    symbolTableManager.setIRValue(ident, globalDecl);
                } else {
                    LocalDecl localDecl = new LocalDecl();
                    new Store(initVal.toIR(), localDecl);

                    symbolTableManager.setIRValue(ident, localDecl);
                }
            }
        } else if (firstDim != 0 && secondDim == 0) {
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(firstDim);
            symbolTableManager.varDecl(ident, true, 1, lens);
            if (IRManager.getInstance().inGlobalDecl()) {
                ArrayInitializer arrayInitializer = (ArrayInitializer) initVal.toIR();
                GlobalDecl globalDecl = new GlobalDecl(arrayInitializer);
                symbolTableManager.arrayInit(ident, arrayInitializer);
                symbolTableManager.setIRValue(ident, globalDecl);
            } else {
                LocalDecl localDecl = new LocalDecl(firstDim);
                ArrayList<Value> vals = ((ArrayInitializer) initVal.toIR()).getVals();
                for (int i = 0; i < firstDim; i++) {
                    Value addr = new GetElementPtr(localDecl, i);
                    new Store(vals.get(i), addr);
                }
                symbolTableManager.setIRValue(ident, localDecl);
            }
        } else {
            ArrayList<Integer> lens = new ArrayList<>();
            lens.add(firstDim);
            lens.add(secondDim);
            symbolTableManager.varDecl(ident, true, 2, lens);
            if (IRManager.getInstance().inGlobalDecl()) {
                ArrayInitializer arrayInitializer = (ArrayInitializer) initVal.toIR();
                GlobalDecl globalDecl = new GlobalDecl(arrayInitializer);
                symbolTableManager.arrayInit(ident, arrayInitializer);
                symbolTableManager.setIRValue(ident, globalDecl);
            } else {
                LocalDecl localDecl = new LocalDecl(firstDim * secondDim);
                ArrayList<Value> vals = ((ArrayInitializer) initVal.toIR()).getVals();
                for (int i = 0; i < vals.size(); i++) {
                    Value addr = new GetElementPtr(localDecl, i);
                    new Store(vals.get(i), addr);
                }
                symbolTableManager.setIRValue(ident, localDecl);
            }
        }

        return null;
    }
}
