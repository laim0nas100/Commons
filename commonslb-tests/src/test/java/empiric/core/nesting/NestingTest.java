package empiric.core.nesting;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.misc.NestingHelper.N;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class NestingTest {

    

    @Test
    public void testNest() throws Exception {

        N<String, Integer> of
                = N.ofk("", 120,
                        N.of(10),
                        N.of(40),
                        N.of(50,
                                N.of(25,
                                        10, 15),
                                N.of(25,
                                        10, 15)
                        ),
                        N.of(20,
                                10,
                                10
                        )
                );

        Log.println(of.collectLeafs());
        Log.print(of.toNestedString());
        Log.await(1, TimeUnit.DAYS);
    }

}
