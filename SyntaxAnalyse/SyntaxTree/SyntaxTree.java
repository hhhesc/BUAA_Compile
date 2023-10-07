package SyntaxAnalyse.SyntaxTree;

import CompileError.CompileException;
import LexicalAnalyse.Words.Word;

import java.util.LinkedList;

public class SyntaxTree {
    private final SyntaxTreeNode root;
    private SyntaxTreeNode curNode;

    private final LinkedList<SyntaxTreeNode> nodeStack = new LinkedList<>();

    public SyntaxTree() {
        root = NodeBuilder.buildIntermediateNode(SyntaxNodeType.CompUnit, null);
        curNode = root;
    }

    public void addIntermediateNode(SyntaxNodeType type) {
        SyntaxTreeNode child = NodeBuilder.buildIntermediateNode(type, curNode);
        curNode.addChild(child);
        curNode = child;
    }

    public void addLeaf(Word token) {
        SyntaxTreeNode child = new SyntaxTreeNode(curNode, token);
        curNode.addChild(child);
    }

    public void moveToParent() {
        curNode.adjust();
        curNode = curNode.getParent();
    }

    public void record() {
        nodeStack.push(curNode);
        curNode.record();
    }

    public void back() {
        curNode = nodeStack.pop();
        curNode.back();
    }

    public void release() {
        SyntaxTreeNode node = nodeStack.pop();
        node.release();
    }

    public String toString() {
        return root.toString();
    }

}
