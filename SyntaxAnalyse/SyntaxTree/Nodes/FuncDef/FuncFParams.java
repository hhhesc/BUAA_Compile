package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.Function.Param;
import IntermediatePresentation.IRManager;
import IntermediatePresentation.Instruction.Load;
import IntermediatePresentation.Instruction.LocalDecl;
import IntermediatePresentation.Instruction.Store;
import IntermediatePresentation.Value;
import IntermediatePresentation.ValueType;
import LexicalAnalyse.Words.Word;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.LVal;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class FuncFParams extends SyntaxTreeNode {
    public FuncFParams(SyntaxTreeNode parent) {
        super(SyntaxNodeType.FuncFParams, parent);
    }

    public ArrayList<Integer> getParamDims() {
        ArrayList<Integer> params = new ArrayList<>();
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParam) {
                params.add(child.getDim());
            }
        }
        return params;
    }


    public void checkError() {
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParam param) {
                if (symbolTableManager.notDeclaredInCurLevel(param.getIdent())) {
                    symbolTableManager.varDecl(param.getIdent(), false, param.getDim(), new ArrayList<>());
                } else {
                    ErrorManager.addError('b', param.getFirstLeafLineNumber());
                }
            }
        }
    }

    public Value toIR() {
        Param param = new Param();
        for (SyntaxTreeNode child : children) {
            if (child instanceof FuncFParam) {
                param.addParam(child.toIR());
            }
        }
        return param;
    }
}
