package LexicalAnalyse;

import LexicalAnalyse.Words.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Lexer {
    private int tokenIndex = 0;
    private final ArrayList<Word> tokens = new ArrayList<>();
    private final LinkedList<Integer> posStack = new LinkedList<>();

    public Lexer(String path) throws IOException {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        WordAnalyser wordAnalyser = new WordAnalyser();
        boolean inAnnotation = false;
        boolean inString = false;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < line.length() - 1; i++) {
                if (inAnnotation) {
                    if (line.charAt(i) == '*' && line.charAt(i + 1) == '/') {
                        inAnnotation = false;
                        i++;
                    }
                } else {
                    if (line.charAt(i) == '/' && line.charAt(i + 1) == '/' && !inString) {
                        break;
                    } else if (line.charAt(i) == '/' && line.charAt(i + 1) == '*' && !inString) {
                        inAnnotation = true;
                        i++;
                    } else if (line.charAt(i) == '"') {
                        inString = !inString;
                        sb.append('"');
                        if (i == line.length() - 2) {
                            sb.append(line.charAt(i + 1));
                        }
                    } else {
                        sb.append(line.charAt(i));
                        if (i == line.length() - 2) {
                            sb.append(line.charAt(i + 1));
                        }
                    }
                }
            }

            if (line.length() == 1 && !inAnnotation) {
                sb.append(line.charAt(0));
            }
            tokens.addAll(wordAnalyser.getTokens(sb.toString(), lineNumber));
            if (tokens.contains(new Word("Err", lineNumber))) {
                return;
            }
        }
        br.close();
    }

    public String getToken() {
        try {
            return tokens.get(tokenIndex).getTokenType();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public String getSrc() {
        try {
            return tokens.get(tokenIndex).getSrcStr();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public Word get() {
        try {
            return tokens.get(tokenIndex);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public Integer getLineNumber() {
        try {
            return tokens.get(tokenIndex).getLineNumber();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public Integer getLastLineNumber() {
        try {
            return tokens.get(tokenIndex - 1).getLineNumber();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public void next() {
        tokenIndex++;
    }

    public void record() {
        posStack.push(tokenIndex);
    }

    public void back() {
        if (posStack.size() > 0) {
            tokenIndex = posStack.pop();
        } else {
            tokenIndex--;
        }
    }

    public void release() {
        posStack.pop();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Word word : tokens) {
            sb.append(word.getTokenType()).append(" ").append(word.getSrcStr()).append("\n");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
