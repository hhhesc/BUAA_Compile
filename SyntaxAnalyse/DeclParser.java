package SyntaxAnalyse;

import ErrorHandler.CompileError.CompileException;
import ErrorHandler.ErrorManager;
import LexicalAnalyse.Lexer;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;

public class DeclParser extends Parser {
    public DeclParser(Lexer lexer) {
        super(lexer);
    }

    public void parse() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Decl);
        if (lexer.getSrc().equals("const")) {
            constDecl();
        } else {
            varDecl();
        }
        if (lexer.getSrc().equals("(")) {
            throw new CompileException(lexer.getLineNumber());
        }
        buildDone();
    }

    private void constDecl() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ConstDecl);
        buildLeaf("const");
        bType();
        constDef();
        while (lexer.getSrc().equals(",")) {
            buildLeaf(",");
            constDef();
        }
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }


    private void constDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ConstDef);
        ident();
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
        buildLeaf("=");
        constInitVal();
        buildDone();
    }


    private void constInitVal() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ConstInitVal);
        try {
            record();
            constExp();
            release();
        } catch (CompileException e) {
            back();
            buildLeaf("{");
            if (!lexer.getSrc().equals("}")) {
                constInitVal();
                while (lexer.getSrc().equals(",")) {
                    buildLeaf(",");
                    constInitVal();
                }
            }
            buildLeaf("}");
        }
        buildDone();
    }

    private void varDecl() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.VarDecl);
        bType();
        varDef();
        while (lexer.getSrc().equals(",")) {
            buildLeaf(",");
            varDef();
        }
        try {
            record();
            buildLeaf(";");
            release();
        } catch (CompileException e) {
            back();
            ErrorManager.addError('i', lexer.getLastLineNumber());
        }
        buildDone();
    }

    private void varDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.VarDef);
        ident();
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
        if (lexer.getSrc().equals("=")) {
            buildLeaf("=");
            initVal();
        }
        buildDone();
    }

    private void initVal() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.InitVal);
        if (lexer.getSrc().equals("{")) {
            buildLeaf("{");
            if (!lexer.getSrc().equals("}")) {
                initVal();
                while (lexer.getSrc().equals(",")) {
                    buildLeaf(",");
                    initVal();
                }
            }
            buildLeaf("}");
        } else {
            exp();
        }
        buildDone();
    }
}
