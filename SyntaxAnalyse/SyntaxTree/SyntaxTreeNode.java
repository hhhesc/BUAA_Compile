package SyntaxAnalyse.SyntaxTree;

import LexicalAnalyse.Words.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class SyntaxTreeNode {
    protected final Word token;
    protected ArrayList<SyntaxTreeNode> children;
    protected SyntaxTreeNode parent;

    protected LinkedList<Integer> childrenStack = new LinkedList<>();
    protected SyntaxNodeType type;

    public SyntaxTreeNode(SyntaxNodeType type, SyntaxTreeNode parent) {
        children = new ArrayList<>();
        token = null;
        this.parent = parent;
        this.type = type;
    }

    public SyntaxTreeNode(SyntaxTreeNode parent, Word token) {
        this.children = new ArrayList<>();
        this.token = token;
        this.parent = parent;
        type = SyntaxNodeType.Leaf;
    }

    public ArrayList<SyntaxTreeNode> getChildren() {
        return children;
    }

    public SyntaxTreeNode getParent() {
        return parent;
    }

    public void addChild(SyntaxTreeNode child) {
        children.add(child);
    }

    public boolean isLeaf() {
        return type == SyntaxNodeType.Leaf;
    }

    public void record() {
        childrenStack.push(children.size());
    }

    public void back() {
        int childrenSize = childrenStack.pop();
        ArrayList<SyntaxTreeNode> newChildren = new ArrayList<>();
        for (int i = 0; i < childrenSize; i++) {
            SyntaxTreeNode child = children.get(i);
            newChildren.add(child);
        }
        children = newChildren;
    }

    public void release() {
        childrenStack.pop();
    }

    public void adjust(){

    }

    private static final HashSet<SyntaxNodeType> nodeShouldNotPrint = new HashSet<>();

    static {
        nodeShouldNotPrint.add(SyntaxNodeType.BlockItem);
        nodeShouldNotPrint.add(SyntaxNodeType.Decl);
        nodeShouldNotPrint.add(SyntaxNodeType.BType);
        nodeShouldNotPrint.add(SyntaxNodeType.ReturnStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.TheForStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.IntConst);
        nodeShouldNotPrint.add(SyntaxNodeType.IfStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.PrintfStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.GetIntStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.BreakStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.ContinueStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.LValAssignGetIntStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.LValAssignExpStmt);
        nodeShouldNotPrint.add(SyntaxNodeType.Ident);
        nodeShouldNotPrint.add(SyntaxNodeType.FormatString);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isLeaf()) {
            return token.getTokenType() + " " + token.getSrcStr() + "\n";
        } else {
            for (SyntaxTreeNode child : children) {
                sb.append(child.toString());
            }
            if (!nodeShouldNotPrint.contains(type)) {
                sb.append("<").append(type).append(">").append("\n");
            }
        }
        return sb.toString();
    }
}
