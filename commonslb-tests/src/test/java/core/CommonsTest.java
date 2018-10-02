package core;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import lt.lb.commons.ArrayBasedCounter;
import lt.lb.commons.Log;
import lt.lb.commons.filemanaging.FileReader;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.F;
import org.junit.*;

/**
 *
 * @author laim0nas100
 */
public class CommonsTest {

    public CommonsTest() {
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
    
    static{
        Log.instant = true;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    ArrayBasedCounter abc = new ArrayBasedCounter(10, 0);
    BigInteger bigInt = BigInteger.ZERO;

    public void benchCounter() throws InterruptedException {
        long time1, time2;
        time1 = System.currentTimeMillis();

        long times = 10000000;
        long i = 0;
        while (i++ < times) {
            abc.inc(1);

        }

        time1 = System.currentTimeMillis() - time1;
        time2 = System.currentTimeMillis();
        i = 0;
        while (i++ < times) {
            bigInt = bigInt.add(BigInteger.ONE);

        }
        time2 = System.currentTimeMillis() - time2;
        Log.print("Done");

        Log.print(time1, time2);
        Thread.sleep(500);
    }
    
//    @Test
    public void testFilterDistinct(){
        Collection<Integer> collection = new LinkedList<>(Arrays.asList(1,1,2,3,4,5,6,6,7,8,9,10));
        
//        List<Integer> filterParallel = F.filterParallel(collection, n -> n%2 == 0, new DisposableExecutor(4));
//        Log.print("Removed after filter",filterParallel);
        Log.print("Left after filter",collection);
        List<Integer> filterDistinct = F.filterDistinct(collection, Equator.valueHashEquator(n -> n%2 == 0? -1 : n.hashCode()));
        
        Log.print("Removed filter distinct",filterDistinct);
        Log.print("Left",collection);
        
        
        
        
    }
    
    @Test
    public void readFile() throws Exception{
            String desktop = "C:\\Users\\Lemmin\\Desktop\\";
            String url = desktop+"myFile.txt";
            ArrayList<String> readFromFile = FileReader.readFromFile(url, "#", "/*", "*/");
            Log.instant = true;
            Log.printLines(readFromFile);
    }
    
//    @Test
    public void convertToArff(){
        
        
        Log.instant = true;
        String desktop = "C:\\Users\\Lemmin\\Desktop\\";
        String relationTitle = "SomeTitle";
        
        
        F.unsafeRun(()->{
            ArrayList<String> readFromFile = FileReader.readFromFile(desktop+"raw.txt");
            int colCount =  readFromFile.get(0).split(",").length;
            Log.print(colCount);
            
            ArrayList<String> arff = new ArrayList<>();
            arff.add("@relation "+relationTitle);
            arff.add("");
            for(int i = 1; i <= colCount; i++){
                String col = "@attribute col"+i+"   NUMERIC";
                arff.add(col);
            }
            arff.add("");
            arff.add("@data");
            arff.addAll(readFromFile);
            FileReader.writeToFile(desktop+"output.arff", arff);
            
        });
        
    } 
}
