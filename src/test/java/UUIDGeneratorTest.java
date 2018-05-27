/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import lt.lb.commons.Log;
import lt.lb.commons.UUIDgenerator;
import org.junit.*;

/**
 *
 * @author Lemmin
 */
public class UUIDGeneratorTest {

    public UUIDGeneratorTest() {
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
    @Test
    public void testUUID() throws InterruptedException {

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            String next = UUIDgenerator.nextUUID("TEST");
            list.add(next);
        }

        for (String n : list) {
            Log.print(n);
        }
//        Tracer.get("").dump(list);

        Thread.sleep(1000);

    }
}
