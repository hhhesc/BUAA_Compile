package SyntaxAnalyse;

import CompileError.CompileException;
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
        if (!lexer.getSrc().equals(")")) {
            funcFParams();
        }
        buildLeaf(")");
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
            buildLeaf("]");
            while (lexer.getSrc().equals("[")) {
                buildLeaf("[");
                constExp();
                buildLeaf("]");
            }
        }
        buildDone();
    }

    public void mainFuncDef() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.MainFuncDef);
        buildLeaf("int");
        buildLeaf("main");
        buildLeaf("(");
        buildLeaf(")");
        block();
        buildDone();
    }
}
