package SyntaxAnalyse.SyntaxTree.Nodes.Utils;

import ErrorHandler.CompileError.CompileException;
import ErrorHandler.ErrorManager;
import IntermediatePresentation.Value;
import LexicalAnalyse.Words.Word;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

public class Ident extends SyntaxTreeNode {
    private Word ident = null;

    public Ident(SyntaxTreeNode parent) {
        super(SyntaxNodeType.Ident, parent);
    }

    public Word getIdent() {
        ident = children.get(0).getWord();
        return ident;
    }

    public void checkError() {
        if (ident == null) {
            ident = getIdent();
        }
        //位于声明语句时，父节点都已经进行过checkError，已声明的变量一定存在符号表的对应项
        if (symbolTableManager.notDeclared(ident.getSrcStr())) {
            ErrorManager.addError('c', ident.getLineNumber());
        }
    }

    public Value toIR(){
        ident = children.get(0).getWord();
        return symbolTableManager.getIRValue(ident.getSrcStr());
    }
}
