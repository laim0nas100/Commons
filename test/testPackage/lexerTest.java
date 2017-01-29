/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.Containers.SelfSortingMap;
import LibraryLB.FileManaging.FileReader;
import LibraryLB.Parsing.Lexer;
import LibraryLB.Parsing.Token;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Lemmin
 */
public class lexerTest {
    
    
    public lexerTest() {
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
//    @Test
    public void testWhitespace(){
        Character c = null;
        System.out.println(Character.isWhitespace(c));
    }
    @Test
    public void testNewMap() throws IOException, Lexer.NoSuchLexemeException, Lexer.StringNotTerminatedException{
        SelfSortingMap map = new SelfSortingMap(new Comparator() {
            @Override
            public int compare(Object t, Object t1) {
                String s1 = (String)t;
                String s2 = (String)t1;
                int len = s2.length() - s1.length();
                if(len ==0){
                    len = 1;
                }
                return len;
            }
        }); 
        map.put("2", "ORr");
        map.put("1", "AND");
       
        
//        System.out.println(map.getOrderedList());
        Collection<String> list = FileReader.readFromFile("1", "//", "/*", "*/");
        System.out.println("RAW");
        for(String line:list){
            System.out.println(line);
        }
        System.out.println("\n");
        Lexer lexer = new Lexer(list);
        lexer.prepareForStrings("\"", "\"", "\\");
        lexer.addToken("int","float","bool","string");
        lexer.addToken("def","return");
        lexer.addToken("(",")","[","]","{","}");
        lexer.addToken("!","=",";");
        lexer.addToken("*","/","+","-");
        lexer.addToken("[]");
        lexer.addToken("==","!=","+=","-=","*=","/=");
        lexer.addToken("<",">","<=",">=");
        lexer.skipWhitespace = true;
        while(true){
            Token t = lexer.getNextToken();
            if(t==null){
                break;
            }else{
                System.out.println(t);
            }
        }
//        Collection<Token> remainingTokens = lexer.getRemainingTokens();
//        for(Token tok:remainingTokens){
//            System.out.println(tok);
//        }
        
                
    }
    public void testLexer(){
        
    }
}
