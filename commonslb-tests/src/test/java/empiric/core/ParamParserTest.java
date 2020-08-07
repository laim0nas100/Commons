package empiric.core;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lt.lb.commons.Log;
import lt.lb.commons.io.TextFileIO;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.parsing.LexerWithStrings;
import lt.lb.commons.parsing.Token;
import empiric.experimental.TokenFiniteAutomata.TGraph;
import empiric.experimental.TokenFiniteAutomata.TKeywordNode;
import empiric.experimental.TokenFiniteAutomata.TLiteralNode;
import empiric.experimental.TokenFiniteAutomata.TNode;
import empiric.experimental.TokenFiniteAutomata.TNumberNode;
import empiric.experimental.TokenFiniteAutomata.TraversedResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author laim0nas100
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
    }
    /*
     *
     */

    public TGraph getLiteralGraph() {
        TGraph literalGraph = new TGraph("Literal graph");

        TNode n1 = new TLiteralNode(true);
        TNode n2 = new TKeywordNode(".", false, true);
        n1.linkTo(n2);
        n2.linkTo(n1);
        literalGraph.beginNode = n1;
        return literalGraph;
    }

    public TGraph getFetchGraph() {
        TGraph fetchGraph = new TGraph("Fetch graph");

        TNode n1 = new TKeywordNode("${", false, false);

        TNode n2 = new TLiteralNode(false);
        TNode n3 = new TKeywordNode(".", false, true);
        TNode n4 = new TKeywordNode("}", true, false);
        n1.linkTo(n2);
        n2.linkTo(n3);
        n2.linkTo(n4);
        n3.linkTo(n2);

        fetchGraph.beginNode = n1;
        return fetchGraph;
    }

    public TGraph getNumberGraph() {
        TGraph numberGraph = new TGraph("Number graph");

        TNode n1 = new TNumberNode(true);
        TNode n2 = new TKeywordNode(".", false, true);
        TNode n3 = new TNumberNode(true);

        n1.linkTo(n2);
        n2.linkTo(n3);

        numberGraph.beginNode = n1;
        return numberGraph;
    }

    public TGraph getArrayStartGraph() {
        TGraph arrayStart = new TGraph("ArrayStart");
        arrayStart.beginNode = new TKeywordNode("[", true, true);

        TGraph arrayEnd = new TGraph("ArrayEnd");
        arrayEnd.beginNode = new TKeywordNode("]", true, true);

        arrayStart.connect(arrayEnd);

        TGraph comma = new TGraph("Comma");
        comma.beginNode = new TKeywordNode(",", true, true);

        TGraph literalGraph = this.getLiteralGraph();
        arrayStart.connect(literalGraph);
        literalGraph.connect(arrayEnd);
        literalGraph.connect(comma);
        comma.connect(literalGraph);

        TGraph fetchGraph = this.getFetchGraph();
        arrayStart.connect(fetchGraph);
        fetchGraph.connect(arrayEnd);
        fetchGraph.connect(comma);
        comma.connect(fetchGraph);
        return arrayStart;
    }

    public TGraph getEqGraph() {
        TNode eqNode = new TKeywordNode("=", true, false);

        TGraph eq = new TGraph("Eq node");
        eq.beginNode = eqNode;

        //basic assignment
        TGraph literalGraph = this.getLiteralGraph();
        TGraph numberGraph = this.getNumberGraph();
        TGraph fetchGraph = this.getFetchGraph();
        TGraph arrayGraph = this.getArrayStartGraph();

        eq.connect(literalGraph);
        eq.connect(numberGraph);
        eq.connect(fetchGraph);
        eq.connect(arrayGraph);

        return eq;
    }

    public TGraph getStructStartGraph() {
        TGraph structStart = new TGraph("StructStart");
        structStart.beginNode = new TKeywordNode("{", true, true);

        TGraph structEnd = new TGraph("StructEnd");
        structEnd.beginNode = new TKeywordNode("}", true, true);

        TGraph eqGraph = getEqGraph();

        structStart.connect(structEnd);
        structStart.connect(eqGraph);
        eqGraph.connectAtEnd(structEnd);

        return structStart;

    }

//    @Test
    public void ok() throws Exception {
        Log.main().async = false;
        Log.main().display = true;
        String url = "C:\\MyWorkspace\\Commons\\fileToRead.txt";
        Collection<String> readFromFile = TextFileIO.readFromFile(url, "//", "/*", "*/");

        Log.printLines(readFromFile);
        LexerWithStrings lex = new LexerWithStrings();
        lex.addKeywordBreaking("=", "{", "}", "[", "]", ",", ".", "${");
        lex.resetLines(readFromFile);
        lex.setSkipWhitespace(true);
        for (Token token : lex.getRemainingTokens()) {
            Log.print(token);
        }
        Log.print("After parsing");

        MyParser p = new MyParser();
        lex.reset();
        p.tokens = lex.getRemainingTokens();

        TGraph literalGraph = this.getLiteralGraph();
        TGraph eqGraph = this.getEqGraph();
        literalGraph.connect(eqGraph);

        // structs
        TGraph structStart = new TGraph("StructStart");
        structStart.beginNode = new TKeywordNode("{", true, true);

        TGraph structEnd = new TGraph("StructEnd");
        structEnd.beginNode = new TKeywordNode("}", true, true);

        structStart.connect(literalGraph);

        structEnd.connect(structEnd);

        eqGraph.connect(structStart);
        eqGraph.connectAtEnd(structEnd);

        List<List<TraversedResult>> globalList = new ArrayList<>();
        do {
            Log.print("New iteration");
            List<TraversedResult> list = new ArrayList<>();
            literalGraph.fullTraverse(p, list);
            globalList.add(list);
        } while (p.hasNext());
        Log.print("RESULTS");
        Log.printLines(globalList);
        Log.main().display = true;

//        Log.flushBuffer();
//        Log.await(1, TimeUnit.HOURS);
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

    public static class MyParser implements ReadOnlyIterator<Token> {

        public Props global = new Props();

        public ArrayList<Token> tokens = new ArrayList<>();
        public int current = 0;

        @Override
        public boolean hasNext() {
            return current + 1 < tokens.size();
        }

        @Override
        public Token next() {
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

        @Override
        public Integer getCurrentIndex() {
            return current;
        }

        @Override
        public void close() {
        }

    }

}

