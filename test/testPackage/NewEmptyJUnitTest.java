/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.FileManaging.AutoBackupMaker;
import LibraryLB.FileManaging.FileReader;
import LibraryLB.Log;
import LibraryLB.NodeMapAPI;
import LibraryLB.NodeMapAPI.DataMap;
import LibraryLB.NodeMapAPI.Link;
import LibraryLB.NodeMapAPI.Node;
import LibraryLB.Containers.ParametersMap;
import LibraryLB.Parsing.StringOperations;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laimonas Beniušis
 */
public class NewEmptyJUnitTest {
    
    public NewEmptyJUnitTest() {
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
    
    
    public void test4(){
        DataMap map = new DataMap();
        {
            Link l = new Link(1,2,100,0);
            map.addLink(l);
        }
        {
            Link l = new Link(1,3,0,0);
            map.addLink(l);
        }
        {
            Link l = new Link(3,2,0,0);
            map.addLink(l);
        }
        {
            Link l = new Link(4,2,0,0);
            map.addLink(l);
        }
        {
            Link l = new Link(4,3,0,0);
            map.addLink(l);
        }
        map.debugPrint();
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        System.err.println(map.cheapestPath(1,4));
        System.err.println(NodeMapAPI.pathValue(list, map));
    }
    
    public void test3(){
        DataMap map = new DataMap();
        {
            Node node = new Node(1);
            node.addLink(2, 1);
            node.addLink(3, 2);
            node.addLink(4, 1);
            map.addNode(node);
        }
        {
            Node node = new Node(2);
            node.addLink(1, 1);
            node.addLink(3, 1);
            node.addLink(4, 2);
            map.addNode(node);
        }
        {
            Node node = new Node(3);
            node.addLink(2, 1);
            node.addLink(1, 2);
            node.addLink(4, 1);
            map.addNode(node);
        }
        {
            Node node = new Node(4);
            node.addLink(1, 1);
            node.addLink(3, 1);
            node.addLink(2, 2);
            map.addNode(node);
        }
        map.debugPrint();
        NodeMapAPI.TreeDataMap tree = NodeMapAPI.makeMinimalSpanningTree(map);
        tree.debugPrint();
        //System.err.println(NodeMapAPI.pathValue(cheapestPath, map));
        
    }
    public void test2() throws IOException, InterruptedException{
        AutoBackupMaker BM = new AutoBackupMaker(5,System.getProperty("user.dir")+File.separator+"BUP","YYYY-MM-dd HH.mm.ss");
        Collection<Runnable> makeNewCopy = BM.makeNewCopy("E:/T1/tt.txt","E:/T1/tt1.txt");
        makeNewCopy.forEach(action ->{
            action.run();
        });
        BM.cleanUp().run();
    }
    
    public void test1() throws IOException{
        String dir = System.getProperty("user.dir");
        ArrayList<String> list = new ArrayList(FileReader.readFromFile(dir+"/Param.txt"));
        FileReader.writeToFile(dir+"/Log.txt", list);
        ParametersMap map = new ParametersMap(list,"=");
        map.map.values().forEach(param->{
            Log.print(param.key,"|",param.object);
        });
        Long i = new Long((int) map.defaultGet("int.1", 10));
        Double d = (Double) map.defaultGet("double.1", 0.5);
        Log.println(i,d);
        
    }
    @Test
    public void hello() {
        //StringInfo info = new StringInfo("LAbas");
        //Log.write(info);
        long l1 = System.currentTimeMillis();
//        Log.disable = false;
        Log.print();
        Log.println(StringOperations.correlationRatio("321", "54321"));
        Log.println(StringOperations.correlationRatio("321", "654321"));
        Log.println(StringOperations.correlationRatio("321", "7654321"));
        Log.println(StringOperations.correlationRatio("321", "87654321"));
        Log.println(StringOperations.correlationRatio("4321", "987654321"));
        Log.print();
        Log.println(StringOperations.correlationRatio("HELLO.txt", "hello.txt"));
        Log.println(StringOperations.correlationRatio("hello.logas", "hello.textas"));
        Log.println(StringOperations.correlationRatio("nesusija9", "987654321"));
        Log.println(StringOperations.correlationRatio("EDEN - Fumes", "EDEN - Fume1"));
        Log.println(StringOperations.correlationRatio("123456text.txt", "123456text.txt"));
//        Log.print();
        System.out.println("Duration "+ (System.currentTimeMillis()-l1));
        
    }
}
