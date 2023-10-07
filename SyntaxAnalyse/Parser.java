package SyntaxAnalyse;

import CompileError.CompileException;
import LexicalAnalyse.Lexer;
import LexicalAnalyse.Words.Ident;
import LexicalAnalyse.Words.KeyWord;
import SyntaxAnalyse.SyntaxTree.SyntaxNodeType;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    protected final Lexer lexer;
    private static final SyntaxTree syntaxTree = new SyntaxTree();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void parse() throws CompileException {
        compUnit();
    }

    private void compUnit() throws CompileException {
        while (true) {
            try {
                record();
                decl();
                release();
            } catch (CompileException e) {
                back();
                break;
            }
        }

        while (true) {
            try {
                record();
                funcDef();
                release();
            } catch (CompileException e) {
                back();
                break;
            }
        }

        (new FuncDefParser(lexer)).mainFuncDef();
    }


    protected void buildLeaf(String reg) throws CompileException {
        if (lexer.getToken() == null || !lexer.getSrc().equals(reg)) {
            throw new CompileException(lexer.getLineNumber());
        }
        syntaxTree.addLeaf(lexer.get());
        lexer.next();
    }

    protected void buildLeaf(Pattern pattern) throws CompileException {
        Matcher m = pattern.matcher(lexer.getSrc());
        if (lexer.getToken() == null || !m.matches()) {
            throw new CompileException(lexer.getLineNumber());
        }
        syntaxTree.addLeaf(lexer.get());
        lexer.next();
    }

    protected void buildIntermediateNode(SyntaxNodeType type) {
        syntaxTree.addIntermediateNode(type);
    }

    protected void buildDone() {
        syntaxTree.moveToParent();
    }

    protected void record() {
        lexer.record();
        syntaxTree.record();
    }

    protected void back() {
        lexer.back();
        syntaxTree.back();
    }

    protected void release() {
        syntaxTree.release();
    }

    protected void ident() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Ident);
        Matcher isKeyWord = KeyWord.pattern.matcher(lexer.getSrc());
        if (isKeyWord.matches()) {
            throw new CompileException(-1);
        }
        buildLeaf(Ident.pattern);
        buildDone();
    }

    protected void block() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.Block);
        buildLeaf("{");
        while (!lexer.getSrc().equals("}")) {
            blockItem();
        }
        buildLeaf("}");
        buildDone();
    }

    protected void blockItem() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.BlockItem);
        try {
            record();
            decl();
            release();
        } catch (CompileException e) {
            back();
            stmt();
        }
        buildDone();
    }

    protected void cond() throws CompileException {
        (new ExpParser(lexer)).cond();
    }

    protected void decl() throws CompileException {
        (new DeclParser(lexer)).parse();
    }

    protected void exp() throws CompileException {
        (new ExpParser(lexer)).parse();
    }

    protected void constExp() throws CompileException {
        (new ExpParser(lexer)).parseConst();
    }

    protected void funcDef() throws CompileException {
        (new FuncDefParser(lexer)).parse();
    }

    protected void stmt() throws CompileException {
        (new StmtParser(lexer)).parse();
    }

    protected void lVal() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.LVal);
        ident();
        while (lexer.getSrc().equals("[")) {
            buildLeaf("[");
            exp();
            buildLeaf("]");
        }
        buildDone();
    }

    protected void bType() throws CompileException {
        buildIntermediateNode(SyntaxNodeType.BType);
        buildLeaf("int");
        buildDone();
    }

    public String toString() {
        return syntaxTree.toString();
    }
}
