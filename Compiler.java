import ErrorHandler.ErrorManager;
import IntermediatePresentation.Module;
import LexicalAnalyse.Lexer;
import SyntaxAnalyse.Parser;
import SyntaxAnalyse.SyntaxTree.SyntaxTree;
import TargetCode.MipsFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args) throws Exception {
        //词法
        Lexer lexer = new Lexer("testfile.txt");
        //语法
        Parser parser = new Parser(lexer);
        parser.parse();
        SyntaxTree syntaxTree = parser.getSyntaxTree();
        //错误处理
        syntaxTree.checkError();
        ErrorManager.log();

        if (ErrorManager.noError()) {
            //llvm ir
            Module module = syntaxTree.toIR();

            File llvmFile = new File("llvm_ir.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(llvmFile));
            bw.write(module.toString());
            bw.flush();
            bw.close();
            //mips
            MipsFile mips = module.toMipsFile();

            File mipsFile = new File("mips.txt");
            bw = new BufferedWriter(new FileWriter(mipsFile));
            bw.write(mips.toString());
            bw.flush();
            bw.close();
        }
    }
}

