/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.graphtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import lt.lb.commons.Log;
import lt.lb.commons.graphtheory.GLink;
import lt.lb.commons.graphtheory.Orgraph;
import lt.lb.commons.graphtheory.paths.GraphGenerator;
import lt.lb.commons.F;
import lt.lb.commons.misc.rng.RandomDistribution;
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
        Log.main().async = true;
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
        Log.printLines(links);

    }
}
