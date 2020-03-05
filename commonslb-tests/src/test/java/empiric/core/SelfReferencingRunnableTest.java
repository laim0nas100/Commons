package empiric.core;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.threads.executors.FastExecutor;

/**
 *
 * @author laim0nas100
 */
public class SelfReferencingRunnableTest {


    public static void main(String... args) {
        
//        create1();
        create2();
//        create3();
        
    }
    
    
    public static void create3(){
        
        AtomicLong c = new AtomicLong(0);
        Value<Runnable> val = new Value<>();
        Consumer<Runnable> run = r -> {
            if (c.get() % 100 == 0) {
                System.out.println("OH hi " + c.get() / 100);
            }
            c.incrementAndGet();

            F.unsafeRun(() -> {
                Thread.sleep(1);
            });
        };
        
        

    }
    public static void create2(){
        AtomicLong c = new AtomicLong(0);
        FastExecutor exe = new FastExecutor(1);
        Value<Runnable> value = new Value<>();
        Runnable run = ()-> {
            if (c.get() % 100 == 0) {
                System.out.println("OH hi " + c.get() / 100);
            }
            c.incrementAndGet();

            F.unsafeRun(() -> {
                Thread.sleep(0,10);
            });
            
            value.get().run();
//            exe.execute(value.get());
            
        };
        
        value.set(run);
        
        exe.execute(run);

        F.unsafeRun(()->{
            Thread.sleep(10000); // sleep 10 sec
        });
        exe.close();
    }
    
    public static void create1(){
        AtomicLong c = new AtomicLong(0);
        Runnable run = SelfReferencingRunnableTest.create(r -> {
            if (c.get() % 100 == 0) {
                System.out.println("OH hi " + c.get() / 100);
            }
            c.incrementAndGet();

            F.unsafeRun(() -> {
                Thread.sleep(1);
            });
            r.get().run();
        });

        run.run();
    }
    

    public static Runnable create(Consumer<Value<Runnable>> runCons) {
        Value<Runnable> main1 = new Value<>();
        Value<Runnable> main2 = new Value<>();

        final Runnable r1 = ()->{
            runCons.accept(main1);
        };
        
        
        main1.set(r1);
        

        return r1;

    }

}
