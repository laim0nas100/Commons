package core;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;
import lt.lb.commons.Timer;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.threads.Promise;
import lt.lb.commons.threads.UnsafeRunnable;
import org.junit.*;

/**
 *
 * @author laim0nas100
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
            for (int i = 0; i < 100000; i++) {
                list.add(UUIDgenerator.nextUUID(Thread.currentThread().getName()) +" "+ UUIDgenerator.counterUUID(""));
            }
        };

        Promise waiter = new Promise();
        Promise r1 = new Promise(r);
//        Promise r2 = new Promise(r);

        new Thread(r1).start();
//        new Thread(r2).start();

        waiter = waiter.waitFor(r1);

        waiter.run();
        waiter.get();

        for (String n : list) {
            Log.print(n);
        }
//        Tracer.get("").dump(list);

        Thread.sleep(1000);

    }

    private static final String constant = "C";

    public void testUUID2(int iter) {
        Timer timer = new Timer();

        long[] times = new long[3];
        timer.lastStopMillis();
        for (int i = 0; i < iter; i++) {
            UUID.randomUUID();
        }
        times[0] = timer.lastStopMillis();
        for (int i = 0; i < iter; i++) {
            UUIDgenerator.nextUUID(constant);
        }
        times[1] = timer.lastStopMillis();
        for (int i = 0; i < iter; i++) {
            UUIDgenerator.counterUUID("");
        }
        times[2] = timer.lastStopMillis();

        System.out.println(Arrays.asList(ArrayOp.mapLong(times)));
    }

//    @Ignore
//    @Test
    public void t() {
        int times = 10000000;
        testUUID2(times);
        testUUID2(times);

        testUUID2(times);
        testUUID2(times);
    }
    
}
