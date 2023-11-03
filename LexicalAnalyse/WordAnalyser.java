package LexicalAnalyse;

import LexicalAnalyse.Words.FormatString;
import LexicalAnalyse.Words.Ident;
import LexicalAnalyse.Words.KeyWord;
import LexicalAnalyse.Words.Symbol;
import LexicalAnalyse.Words.IntConst;
import LexicalAnalyse.Words.Word;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordAnalyser {
    public ArrayList<Word> getTokens(String srcStr, int lineNumber) {
        int curIndex = 0;
        ArrayList<Word> tokens = new ArrayList<>();
        while (curIndex != srcStr.length()) {
            Matcher matcher = Pattern.compile("\\s+").matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {
                curIndex += matcher.end();
                continue;
            }

            matcher = FormatString.pattern.matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {

                tokens.add((new FormatString(srcStr.substring(curIndex,
                        matcher.end() + curIndex), lineNumber)));
                curIndex += matcher.end();
                continue;
            }

            matcher = KeyWord.pattern.matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {
                if (curIndex + matcher.end() >= srcStr.length()) {
                    tokens.add(new KeyWord(srcStr.substring(curIndex,
                            matcher.end() + curIndex), lineNumber));
                    curIndex += matcher.end();
                    continue;
                }
                char c = srcStr.charAt(curIndex + matcher.end());
                if (!Character.isDigit(c) && !Character.isLetter(c) && c != '_') {
                    tokens.add((new KeyWord(srcStr.substring(curIndex,
                            matcher.end() + curIndex), lineNumber)));
                    curIndex += matcher.end();
                    continue;
                }
            }

            matcher = Symbol.pattern.matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {
                tokens.add((new Symbol(srcStr.substring(curIndex,
                        matcher.end() + curIndex), lineNumber)));
                curIndex += matcher.end();
                continue;
            }

            matcher = IntConst.pattern.matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {
                tokens.add((new IntConst(srcStr.substring(curIndex,
                        matcher.end() + curIndex), lineNumber)));
                curIndex += matcher.end();
                continue;
            }

            matcher = Ident.pattern.matcher(srcStr.substring(curIndex));
            if (matcher.lookingAt()) {
                tokens.add((new Ident(srcStr.substring(curIndex,
                        matcher.end() + curIndex), lineNumber)));
                curIndex += matcher.end();
                continue;
            }

            tokens.add(new Word("Err", lineNumber));
            return tokens;
        }
        return tokens;
    }
}