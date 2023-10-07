import LexicalAnalyse.Lexer;
import SyntaxAnalyse.Parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Compiler {
    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer("testfile.txt");
        Parser parser = new Parser(lexer);
        parser.parse();

        File outputFile = new File("output.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write(parser.toString());
        bw.flush();
        bw.close();
    }
}
