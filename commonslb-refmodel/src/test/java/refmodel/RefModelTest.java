/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package refmodel;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import lt.lb.commons.Log;
import lt.lb.commons.containers.Value;
import lt.lb.commons.refmodel.*;
import lt.lb.commons.refmodel.jpa.*;
import org.junit.*;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class RefModelTest {

    public RefModelTest() {
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
    public static class R1 extends Ref implements RefModel {

        public SingularRef<Date> date;
        public SingularRef<String> name;
        public R1 child;
        public ListRef<String> ownedToys;
        public MapRef<String, Double> toyValues;
        public Value<String> val;
    }

    public static class R2 extends Ref implements RefModel {

        public R1 owner;
        public SingularRef<Long> price;
    }

    @Test
    public void refModelTest() throws Exception {
        Log.instant = true;
        R2 r1 = RefCompiler.compile(R2.class);
        Ref<Date> date = r1.owner.child.child.date;
        Log.print(date, date.getClass());
    }

    public static class SomeEntity {

    }

    public void testEntity(EntityManager em) throws Exception {
        R2 r1 = RefCompiler.compile(R2.class);
        Ref<String> name = r1.owner.name;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SomeEntity> q = cb.createQuery(SomeEntity.class);
        Root<SomeEntity> from = q.from(SomeEntity.class);
        q.select(from);

        Fetch<SomeEntity, String> fetch = r1.owner.child.ownedToys.fetch(from);
        //            .where(cb.greaterThanOrEqualTo(r1.owner.child.date.getPath(from), cb.currentDate()));
    }

}
