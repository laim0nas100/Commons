package empiric.refmodel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.refmodel.*;
import lt.lb.commons.refmodel.jparef.JpaListRef;
import lt.lb.commons.refmodel.jparef.JpaMapRef;
import lt.lb.commons.refmodel.jparef.SingularRef;
import lt.lb.commons.refmodel.maps.ListRef;
import lt.lb.commons.refmodel.maps.MapProvider;
import lt.lb.commons.refmodel.maps.MapRef;
import lt.lb.commons.refmodel.maps.ObjectRef;
import lt.lb.uncheckedutils.SafeOpt;
import org.junit.*;

/**
 *
 * @author laim0nas100
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
        public JpaListRef<String> ownedToys;
        public JpaMapRef<String, Double> toyValues;
        public Value<String> val;
    }

    public static class R2 extends Ref implements RefModel {

        public R1 owner;
        public SingularRef<Long> price;
    }

//    @Test
    public void refModelTest() throws Exception {
        DLog.main().async = false;
        R2 r1 = RefCompiler.compile(R2.class);
        Ref<Date> date = r1.owner.child.child.date;
        System.out.println(date + " " + date.getClass());
    }

    public static class Group extends MapRef {

        public Person groupLeader;
        public ListRef<Person> members;
        public ListRef<Group> nestedGroup;
    }

    public static class Person extends MapRef {

        public ObjectRef<String> name;
        public ObjectRef<String> last_name;
        public ObjectRef<Integer> age;
    }

    @Test
    public void refModelMapTest() throws Exception {

        Group group = RefCompiler.compile(Group.class);

        System.out.println(group.members.at(0).last_name.getRelative());
        System.out.println(group.members.at(10).get());

        System.out.println(group.nestedGroup.at(10).nestedGroup.at(1).get());

        MapProvider hashMap = MapProvider.hashMap(new HashMap<>());
        group.groupLeader.name.write(hashMap, "Alex");
        group.members.at(0).name.write(hashMap, "Lemmin");
        group.members.at(0).age.write(hashMap, 30);
        group.members.at(1).name.write(hashMap, "Jake");
        group.members.at(1).age.write(hashMap, 20);
        group.members.at(2).name.write(hashMap, "Laura");
        group.members.at(2).age.write(hashMap, 25);

        System.out.println(hashMap.getMap());

        Gson gson = new Gson();

        System.out.println(group.members.at(1).name.read(hashMap));

        int size = group.members.size(hashMap);
        System.out.println(size);

        String toJson = gson.toJson(hashMap.getMap());

        System.out.println(toJson);

        group.members.at(2).age.remove(hashMap);

        System.out.println(hashMap.getMap());

        group.members.clear(hashMap);
        System.out.println(hashMap.getMap());

        Map<String,Object> fromJson = gson.fromJson(toJson, new TypeToken<Map<String, Object>>() {
        }.getType());

        System.out.println(fromJson);
        
        MapProvider ofDefault = MapProvider.ofDefault(fromJson);
        SafeOpt<Integer> read = group.members.at(0).age.read(ofDefault);
        int get = read.get();
        System.out.println(get);

    }

    public static class SomeEntity {

    }

    public void testEntity(EntityManager em) throws Exception {
        R2 r1 = RefCompiler.compile(R2.class);
        Ref<String> name = r1.owner.name;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SomeEntity> q = cb.createQuery(SomeEntity.class);
        Root<SomeEntity> from = q.from(SomeEntity.class);
        ListJoin<SomeEntity, String> join = r1.owner.ownedToys.join(from);
        MapJoin<SomeEntity, String, Double> join1 = r1.owner.toyValues.join(from);
        q.select(from);

        Fetch<SomeEntity, String> fetch = r1.owner.child.ownedToys.fetch(from, JoinType.INNER);
        //            .where(cb.greaterThanOrEqualTo(r1.owner.child.date.getPath(from), cb.currentDate()));
    }

}
