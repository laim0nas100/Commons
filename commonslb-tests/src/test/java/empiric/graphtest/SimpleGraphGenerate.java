/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.graphtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import com.github.laim0nas100.commonslb.DLog;
import com.github.laim0nas100.commonslb.graphtheory.GLink;
import com.github.laim0nas100.commonslb.graphtheory.Orgraph;
import com.github.laim0nas100.commonslb.graphtheory.paths.GraphGenerator;
import com.github.laim0nas100.commonslb.F;
import com.github.laim0nas100.commonslb.misc.rng.RandomDistribution;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author laim0nas100
 */
public class SimpleGraphGenerate {

    public SimpleGraphGenerate() {
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
    static {
        DLog.main().async = true;
    }

//    @Test
    public void generateSimple() {
        Orgraph gr = new Orgraph();
        Random r = new Random(10);
        GraphGenerator.generateSimpleConnected(RandomDistribution.dice(() -> r.nextDouble(), 0), gr, 50, () -> 1d);

        ArrayList<GLink> links = new ArrayList<>(gr.links.values());
        Collections.sort(links, (a, b) -> {
            int c = (int) (a.nodeFrom - b.nodeFrom);
            if (c == 0) {
                c = (int) (a.nodeTo - b.nodeTo);
            }

            return c;
        });
        DLog.printLines(links);

    }
}
