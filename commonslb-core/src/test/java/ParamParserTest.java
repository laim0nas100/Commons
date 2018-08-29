/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lt.lb.commons.Log;
import lt.lb.commons.filemanaging.FileReader;
import lt.lb.commons.interfaces.ReadOnlyBidirectionalIterator;
import lt.lb.commons.interfaces.ReadOnlyIterator;
import lt.lb.commons.misc.F;
import lt.lb.commons.parsing.LexerWithStrings;
import lt.lb.commons.parsing.Literal;
import lt.lb.commons.parsing.Token;
import lt.lb.commons.parsing.TokenFiniteAutomata;
import lt.lb.commons.parsing.TokenFiniteAutomata.TGraph;
import lt.lb.commons.parsing.TokenFiniteAutomata.TKeywordNode;
import lt.lb.commons.parsing.TokenFiniteAutomata.TNode;
import lt.lb.commons.reflect.DefaultFieldFactory;
import lt.lb.commons.reflect.FieldFactory;
import lt.lb.commons.reflect.ReflectionPrint;
import lt.lb.commons.reflect.ReflectionUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ParamParserTest {

    public ParamParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    public static void main(String[] str) throws Exception {
        ParamParserTest t = new ParamParserTest();
        t.ok();
    }
    /*
    
     */

    FieldFactory fac = new DefaultFieldFactory();

    @Test
    public void ok() throws Exception {
        Log.instant = true;
        String url = "C:\\MyWorkspace\\Commons\\fileToRead.txt";
        Collection<String> readFromFile = FileReader.readFromFile(url, "//", "/*", "*/");

        Log.printLines(readFromFile);
        LexerWithStrings lex = new LexerWithStrings("");
        lex.addKeyword("=", "{", "}", "[", "]", ",", ".", "${");
        lex.resetLines(readFromFile);
        for (Token token : lex.getRemainingTokens()) {
            Log.print(token);
        }

        MyParser p = new MyParser();
        lex.reset();
        p.tokens = lex.getRemainingTokens();

        TNode eqNode = new TKeywordNode("=", true, false);

        TGraph numberGraph = new TGraph();

        F.run(() -> {
            TNode n1 = new TNode(false);
            TNode n2 = new TKeywordNode(".", false, true);
            TNode n3 = new TNode(true);

            n1.linkTo(n2);
            n2.linkTo(n3);

            numberGraph.beginNode = n1;
        });

        TGraph literalGraph = new TGraph();

        F.run(() -> {
            TNode n1 = new TNode(true);
            TNode n2 = new TKeywordNode(".", false, true);
            n1.linkTo(n2);
            n2.linkTo(n1);
            literalGraph.beginNode = n1;
        });

        TGraph fetchGraph = new TGraph();

        F.run(() -> {
            TNode n1 = new TKeywordNode("${", false,false);
            
            TNode n2 = new TNode(false);
            TNode n3 = new TKeywordNode(".", false, true);
            TNode n4 = new TKeywordNode("}",true,false);
            n1.linkTo(n2);
            n2.linkTo(n3);
            n2.linkTo(n4);
            n3.linkTo(n2);
            
            fetchGraph.beginNode = n1;
        });

        TGraph g = new TGraph();
        g.beginNode = eqNode;

        TokenFiniteAutomata.TraversedResult t1 = literalGraph.traverse(p);
        Log.print("After first advance");

        g.traverse(p);
        Log.print("After second advance");
        TokenFiniteAutomata.TraversedResult t2 = fetchGraph.traverse(p);

        Log.print(t1.getStringResult());
        Log.print(t2.getStringResult());

    }

    public static class Props {

        public Props() {
        }

        public Props(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public String name;
        public String value;
        public Map<String, Props> children = new HashMap<>();

        public boolean hasChildren() {
            return !this.children.isEmpty();
        }

        public boolean contains(String node) {
            return this.children.containsKey(node);
        }

        public Props resolve(LinkedList<String> path) {
            if (path.isEmpty()) {
                return this;
            }
            String nextChild = path.peekFirst();
            if (this.contains(name)) {
                path.pollFirst();
                return this.children.get(nextChild).resolve(path);
            }
            return null;
        }

    }

    public void parseMe(Collection<Token> tokens) {
        for (Token t : tokens) {
            if (t instanceof Literal) {
                Literal lit = F.cast(t);

            }
        }
    }

    public static class MyParser implements ReadOnlyIterator<Token> {

        public Props global = new Props();

        public ArrayList<Token> tokens = new ArrayList<>();
        public int current = 0;

        @Override
        public Boolean hasNext() {
            return current + 1 < tokens.size();
        }

        @Override
        public Token getNext() {
            if (hasNext()) {
                current++;
                return tokens.get(current);
            }
            return null;
        }

        @Override
        public Token getCurrent() {
            if (current < tokens.size() && current >= 0) {
                return tokens.get(current);
            } else {
                return null;
            }
        }

    }

}
