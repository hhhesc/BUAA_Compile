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
                String formatstr = srcStr.substring(curIndex, matcher.end() + curIndex);
                if (!isLegalFormatStr(formatstr)) {
                    tokens.add(new Word("Err", lineNumber));
                }

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

    private boolean isLegalFormatStr(String formatstr) {
        if (!formatstr.startsWith("\"") || !formatstr.endsWith("\"")) {
            return false;
        }
        char[] charArray = formatstr.toCharArray();
        for (int i = 1; i < charArray.length - 1; i++) {
            int cVal = charArray[i];
            if (cVal == 37) {
                if (charArray[i + 1] != 'd') {
                    return false;
                } else {
                    i++;
                    continue;
                }
            }
            if (cVal <= 31 || cVal >= 34 && cVal <= 39 || cVal >= 127) {
                return false;
            } else if (cVal == 92 && charArray[i + 1] != 'n') {
                return false;
            }
        }
        return true;
    }
}