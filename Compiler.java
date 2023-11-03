import ErrorHandler.ErrorManager;
import IntermediatePresentation.Module;
import LexicalAnalyse.Lexer;
import SyntaxAnalyse.Parser;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer("testfile.txt");

        Parser parser = new Parser(lexer);
        parser.parse();
        SyntaxTree syntaxTree = parser.getSyntaxTree();

        syntaxTree.checkError();
        ErrorManager.log();

        Module module = syntaxTree.toIR();

        File outputFile = new File("llvm_ir.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write(module.toString());
        bw.flush();
        bw.close();
    }
}