/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.GraphTheory.Algorithms;
import LibraryLB.GraphTheory.DAG;
import LibraryLB.Log;
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
public class DAGgenerate {
    
    public DAGgenerate() {
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
    @Test
    public void hello() {
        int links = 200000;
        
        
        long time1 = System.currentTimeMillis();
        DAG dag = DAG.generateRandomDAGBetter(links, 50,0.1);
        time1 = System.currentTimeMillis() - time1;
        long time2 = System.currentTimeMillis();
//        dag = DAG.generateRandomDAGNaive(links, 50);
        time2 = System.currentTimeMillis() - time2;
        
        Log.print(dag.toStringLinks());
        Log.print(time1,time2);
        Log.print(Algorithms.containsCycleBFS(dag, null));
        Log.close();
    }
}
