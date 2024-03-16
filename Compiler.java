import ErrorHandler.ErrorManager;
import IntermediatePresentation.Module;
import LexicalAnalyse.Lexer;
import Optimizer.Optimizer;
import SyntaxAnalyse.Parser;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;
import TargetCode.MipsFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws Exception {
        String arg;
        if (args.length == 0) {
            arg = "-o"; //default :-o
        } else {
            arg = args[0];
        }
        //词法
        Lexer lexer = new Lexer("testfile.txt");

        //语法
        Parser parser = new Parser(lexer);
        parser.parse();
        SyntaxTree syntaxTree = parser.getSyntaxTree();

        //错误检查
        syntaxTree.checkError();
        ErrorManager.log();
        if (ErrorManager.hasError()) {
            System.out.println("Error");
            return;
        }

        //优化
        Module module = syntaxTree.toIR();
        if (arg.equals("-o")) {
            Optimizer.instance().optimizeModule(module);
        }
        output("llvm_ir.txt", module);
        MipsFile mips = module.toMipsFile();
        if (arg.equals("-o")) {
            Optimizer.instance().optimizeMips(mips);
        }
        output("mips.txt", mips);
    }

    private static void output(String path, Object o) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        bw.write(o.toString());
        bw.flush();
        bw.close();
    }
}

