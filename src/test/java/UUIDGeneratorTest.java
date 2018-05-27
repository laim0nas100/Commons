/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import lt.lb.commons.Log;
import lt.lb.commons.Threads.Promise;
import lt.lb.commons.Threads.Promise.UnsafeRunnable;
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
    public void testUUID() throws InterruptedException, ExecutionException {

        Collection<String> list = new ConcurrentLinkedDeque<>();

        UnsafeRunnable r = () -> {
            for (int i = 0; i < 10000; i++) {
                String next = UUIDgenerator.nextUUID(Thread.currentThread().getName());
                list.add(next);
            }
        };

        Promise waiter = new Promise();
        Promise r1 = new Promise(r);
        Promise r2 = new Promise(r);

        new Thread(r1).start();
        new Thread(r2).start();

        waiter = waiter.waitFor(r1, r2);

        waiter.run();
        waiter.get();

        for (String n : list) {
            Log.print(n);
        }
//        Tracer.get("").dump(list);

        Thread.sleep(1000);

    }
}
