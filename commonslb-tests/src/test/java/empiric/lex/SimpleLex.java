/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.lex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.io.FileReader;
import lt.lb.commons.parsing.LexerWithStrings;
import lt.lb.commons.parsing.Token;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class SimpleLex {

    @Test
    public void simpleJavaLex() throws Exception {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("javaFile.txt");
        ArrayList<String> readFrom = FileReader.readFrom(resourceAsStream);

        LexerWithStrings lex = new LexerWithStrings();
        lex.setSaveComments(false);
        String[] symbols = {
            ".", ",", "'", ";", "!", ":", "?", "@",
            "[", "]", "(", ")", "{", "}",
            "+", "-", "/", "*", "%", "&&", "||", "=",
            "==", ">", "<", "!=", ">=", "<=",
            "++", "--",
            "=+", "=-", "*=", "/=", "%=",
            "~", "^", "|", "&", ">>", "<<", ">>>",
            "~=", "^=", "|=", "&=", ">>=", "<<=", ">>>=",
            "->", "::"
        };
        String[] visibility = {"private", "protected", "public"};
        String[] primitive = {"void", "boolean", "byte", "char", "short", "int", "long", "float", "double"};
        String[] lexemes = {"null", "true", "false"};
        String[] modifiers = {"abstract", "static", "native", "transient", "volatile", "synchronized", "final", "throws"};
        String[] branching = {"break", "case", "switch", "default", "if", "do", "else", "for", "while", "continue", "return", "try", "catch", "finally", "throw"};
        String[] classes = {"package", "import", "class", "implements", "extends"};
        String[] misc = {"new", "this", "super", "enum", "assert", "instanceof"};

        String[] merged = ArrayOp.merge(visibility, primitive, lexemes, modifiers, branching, classes, misc);
        lex.addKeyword(merged);
        lex.addKeywordBreaking(symbols);
        lex.prepareForComments("//", "/*", "*/");


        lex.resetLines(readFrom);
        Log.printLines(lex.getRemainingTokens());
        Log.await(10, TimeUnit.DAYS);

    }
}
