package SyntaxAnalyse;

import ErrorHandler.CompileError.CompileException;
import ErrorHandler.ErrorManager;
import LexicalAnalyse.Lexer;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;

public class FuncDefParser extends Parser {
    public FuncDefParser(Lexer lexer) {
        super(lexer);
    }

    public void parse() throws CompileException {
        funcDef();
    }

    protected void funcDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FuncDef);
        funcType();
        if (lexer.getSrc().equals("main")) {
            throw new CompileException(lexer.getLineNumber());
        }
        ident();
        buildLeaf("(");
        if (!(lexer.getSrc().equals(")") || lexer.getSrc().equals("{"))) {
            funcFParams();
        }
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        block();
        buildDone();
    }

    private void funcType() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FuncType);
        if (lexer.getSrc().equals("void")) {
            buildLeaf("void");
        } else {
            buildLeaf("int");
        }
        buildDone();
    }

    private void funcFParams() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FuncFParams);
        funcFParam();
        while (lexer.getSrc().equals(",")) {
            buildLeaf(",");
            funcFParam();
        }
        buildDone();
    }

    private void funcFParam() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.FuncFParam);
        bType();
        ident();
        if (lexer.getSrc().equals("[")) {
            buildLeaf("[");
            try {
                record();
                buildLeaf("]");
                release();
            } catch (CompileException e) {
                back();
                ErrorManager.addError('k', lexer.getLastLineNumber());
            }
            while (lexer.getSrc().equals("[")) {
                buildLeaf("[");
                constExp();
                try {
                    record();
                    buildLeaf("]");
                    release();
                } catch (CompileException e) {
                    back();
                    ErrorManager.addError('k', lexer.getLastLineNumber());
                }
            }
        }
        buildDone();
    }

    public void mainFuncDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.MainFuncDef);
        buildLeaf("int");
        buildLeaf("main");
        buildLeaf("(");
        try {
            record();
            buildLeaf(")");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('j', lexer.getLastLineNumber());
        }
        block();
        buildDone();
    }
}
