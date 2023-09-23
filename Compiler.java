import LexicalAnalyse.Lexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("testfile.txt");

        File outputFile = new File("output.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write(lexer.toString());
        bw.flush();
        bw.close();
    }
}
