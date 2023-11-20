package ErrorHandler;


import ErrorHandler.SymbolTable.SymbolTableManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class ErrorManager {
    private static HashMap<Integer, Character> errorList = new HashMap<>();
    private static final LinkedList<HashMap<Integer, Character>> errorListStack = new LinkedList<>();

    public static void addError(char typeCode, int lineNumber) {
        if (!errorList.containsKey(lineNumber)) {
            errorList.put(lineNumber, typeCode);
        }
    }

    public static void log() throws IOException {
        File outputFile = new File("error.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        StringBuilder sb = new StringBuilder();

        ArrayList<Integer> lines = new ArrayList<>(errorList.keySet());
        lines.sort(Comparator.naturalOrder());
        for (Integer line : lines) {
            sb.append(line).append(" ").append(errorList.get(line)).append("\n");
        }

        bw.write(sb.substring(0, Math.max(sb.length() - 1, 0)));
        bw.flush();
        bw.close();
    }

    public static void record() {
        errorListStack.push(new HashMap<>(errorList));
    }

    public static void release() {
        errorListStack.pop();
    }

    public static void back() {
        errorList = errorListStack.pop();
    }

    public static boolean noError() {
        return errorList.isEmpty();
    }
}
