package SyntaxAnalyse.SyntaxTree;

import SyntaxAnalyse.SyntaxTree.Nodes.*;
import SyntaxAnalyse.SyntaxTree.Nodes.Decl.*;
import SyntaxAnalyse.SyntaxTree.Nodes.FuncDef.*;
import SyntaxAnalyse.SyntaxTree.Nodes.Exp.*;
import SyntaxAnalyse.SyntaxTree.Nodes.Stmt.*;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.*;
import SyntaxAnalyse.SyntaxTree.Nodes.Utils.Number;

public class NodeBuilder {
    public static SyntaxTreeNode buildIntermediateNode(SyntaxNodeType type, SyntaxTreeNode parent) {
        return switch (type) {
            case CompUnit -> new CompUnit(parent);
            case Decl -> new Decl(parent);
            case FuncDef -> new FuncDef(parent);
            case MainFuncDef -> new MainFuncDef(parent);
            case ConstDecl -> new ConstDecl(parent);
            case VarDecl -> new VarDecl(parent);
            case BType -> new BType(parent);
            case ConstDef -> new ConstDef(parent);
            case Ident -> new Ident(parent);
            case ConstExp -> new ConstExp(parent);
            case ConstInitVal -> new ConstInitVal(parent);
            case VarDef -> new VarDef(parent);
            case InitVal -> new InitVal(parent);
            case Exp -> new Exp(parent);
            case FuncType -> new FuncType(parent);
            case FuncFParams -> new FuncFParams(parent);
            case Block -> new Block(parent);
            case FuncFParam -> new FuncFParam(parent);
            case BlockItem -> new BlockItem(parent);
            case Stmt -> new Stmt(parent);
            case LVal -> new LVal(parent);
            case Cond -> new Cond(parent);
            case IfStmt -> new IfStmt(parent);
            case TheForStmt -> new ForStmt(parent);
            case ForStmt -> new ForStmtStmt(parent);
            case BreakStmt -> new BreakStmt(parent);
            case ContinueStmt -> new ContinueStmt(parent);
            case FormatString -> new FormatString(parent);
            case GetIntStmt -> new GetIntStmt(parent);
            case PrintfStmt -> new PrintfStmt(parent);
            case AddExp -> new AddExp(parent);
            case LValAssignExpStmt -> new LValAssignExpStmt(parent);
            case LValAssignGetIntStmt -> new LValAssignGetIntStmt(parent);
            case ReturnStmt -> new ReturnStmt(parent);

            case LOrExp -> new LOrExp(parent);
            case PrimaryExp -> new PrimaryExp(parent);
            case Number -> new Number(parent);
            case IntConst -> new IntConst(parent);
            case UnaryExp -> new UnaryExp(parent);
            case FuncRParams -> new FuncRParams(parent);
            case UnaryOp -> new UnaryOp(parent);
            case MulExp -> new MulExp(parent);
            case RelExp -> new RelExp(parent);
            case EqExp -> new EqExp(parent);
            case LAndExp -> new LAndExp(parent);
            default -> null;
        };
    }
}
