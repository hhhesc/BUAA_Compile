package SyntaxAnalyse;

import CompileError.CompileException;
import LexicalAnalyse.Lexer;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;

import javax.swing.plaf.basic.BasicLabelUI;

public class DeclParser extends Parser {
    public DeclParser(Lexer lexer) {
        super(lexer);
    }

    public void parse() throws CompileException {
        if (lexer.getSrc().equals("const")) {
            constDecl();
        } else {
            varDecl();
        }
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
        buildLeaf(";");
        buildDone();
    }


    private void constDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.ConstDef);
        ident();
        while (lexer.getSrc().equals("[")) {
            buildLeaf("[");
            constExp();
            buildLeaf("]");
        }
        buildLeaf("=");
        constInitVal();
        buildDone();
    }


    private void constInitVal() throws CompileException {
        //TODO:怎么区分constExp和exp？
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
        buildLeaf(";");
        buildDone();
    }

    private void varDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.VarDef);
        ident();
        while (lexer.getSrc().equals("[")) {
            buildLeaf("[");
            constExp();
            buildLeaf("]");
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
