package SyntaxAnalyse.SyntaxTree.Nodes.FuncDef;

import ErrorHandler.ErrorManager;
import IntermediatePresentation.BasicBlock;
import IntermediatePresentation.Function.MainFunction;
import IntermediatePresentation.Module;
import IntermediatePresentation.Value;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Block;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTreeNode;

import java.util.ArrayList;

public class MainFuncDef extends SyntaxTreeNode {
    public MainFuncDef(SyntaxTreeNode parent) {
        super(SyntaxNodeType.MainFuncDef, parent);
    }

    public void checkError() {
        symbolTableManager.funcDecl(false, "main", new ArrayList<>());
        symbolTableManager.enterBlock();
        super.checkError();
        symbolTableManager.funcDeclEnd();

        Block block = (Block) children.get(children.size() - 1);
        if (block.notExistReturnStmt()) {
            ErrorManager.addError('g',
                    block.getChildren().get(block.getChildren().size() - 1).getWord().getLineNumber());
        }
    }

    public MainFunction toIR() {
        MainFunction mainFunction = new MainFunction();
        symbolTableManager.funcDecl(false, "main", new ArrayList<>());
        symbolTableManager.enterBlock();
        super.toIR();
        symbolTableManager.funcDeclEnd();
        return mainFunction;
    }
}
