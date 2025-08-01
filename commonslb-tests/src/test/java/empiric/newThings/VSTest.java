package empiric.newThings;

import lt.lb.commons.io.serialization.VSManager;
import lt.lb.commons.io.serialization.VersionedChanges;
import lt.lb.commons.io.serialization.VersionedSerialization;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lt.lb.commons.DLog;
import lt.lb.commons.F;
import org.apache.commons.lang3.SerializationUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author laim0nas100
 */
public class VSTest {

    public static enum StudentType {
        FullTime, Partial
    }

    public static class BaseData {

        public ZonedDateTime date = ZonedDateTime.now();
    }

    public static class Data extends BaseData {

        public ZonedDateTime date = ZonedDateTime.now().minusDays(1);
        public BigInteger bigInt = BigInteger.ONE;
        public BigDecimal bigDec = new BigDecimal(10).divide(BigDecimal.valueOf(3), MathContext.DECIMAL32);

        public List<Integer> numbers = new ArrayList<>();

        public String name;

        public Map<String, String> simpleMap = new HashMap<>();

        public Map<String, Student> compValMap = new HashMap<>();

        public List<Student> students = new ArrayList<>();
    }

    public static class Data2 extends BaseData {

        public ZonedDateTime date = ZonedDateTime.now().minusDays(1);
        public BigInteger bigInt = BigInteger.ONE;
        public BigDecimal bigDec = new BigDecimal(10).divide(BigDecimal.valueOf(3), MathContext.DECIMAL32);

        public int[] array;

        public String name;

        public Map<String, String> simpleMap = new HashMap<>();

        public Map<String, StudentNew> compValMap = new LinkedHashMap<>();

        public List<StudentNew> students = new ArrayList<>();
    }

    public static class Student {

        public String firstName;
        public String lName;
        public StudentType sType = StudentType.FullTime;

        public Runnable run = () -> {
            System.out.println(firstName + " " + lName);
        };

        public Student(String firstName, String lName) {
            this.firstName = firstName;
            this.lName = lName;
        }

        public Student() {

        }

    }

    public static class StudentNew {

        public String firstName;
        public String lastName;
        public StudentType sType = StudentType.FullTime;

        public Runnable run = () -> {
            System.out.println(firstName + " " + lastName);
        };

        public StudentNew(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public StudentNew() {

        }

    }

    public static class RunnableDummy {

        public String name = "Hello";
        public Runnable run = () -> {
            System.out.println(name);
        };
    }

    public static void main(String[] args) {
        DLog.main().async = false;
        Data data = new Data();

        data.numbers.add(1);
        data.numbers.add(2);
        data.numbers.add(3);
        Student same = new Student("Tom", "Soyer");
        data.students.add(same);
        data.students.add(new Student("Tom", "Soyer2"));
        data.students.add(new Student("Reginald", "Took"));
        data.students.add(same);

        for (Student stud : data.students) {
//            data.simpleMap.put(stud.firstName, stud.lName);
            data.compValMap.put(stud.firstName, stud);
        }
        VSManager ser = new VSManager();
        ser.includeCustomRefCounting(Student.class);
        ser.includeCustomRefCounting(Data.class);
        ser.includeCustomRefCounting(StudentNew.class, 1);
        ser.includeCustomRefCounting(Data2.class, 1);
        ser.exludeBase(Runnable.class);
        ser.withStringifyType(BigInteger.class, s -> new BigInteger(s));
        ser.withStringifyType(BigDecimal.class, s -> new BigDecimal(s));
        ser.withStringifyType(ZonedDateTime.class, s -> ZonedDateTime.parse(s));

        VersionedSerialization.CustomVSUnit root = ser.serializeRoot(data);

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setTagInspector(tag -> true);
        Yaml yaml = new Yaml(loaderOptions);

        byte[] bytes = SerializationUtils.serialize(root);// write
        VersionedSerialization.CustomVSUnit newRoot = SerializationUtils.deserialize(bytes);//read
        String dump = yaml.dump(newRoot);
        DLog.print(dump);

        ser.addVersionChanger(VersionedChanges.builderVerionInc(Student.class, 0)
                .withFieldRename("lName", "lastName")
                .withTypeChange(StudentNew.class)
        );
        ser.addVersionChanger(VersionedChanges.builderVerionInc(Data.class, 0)
                .withTypeChange(Data2.class)
                .withFieldRefactor("numbers", field -> {//change the list<Integer>  to int[] and  fieldName
                    VersionedSerialization.ArrayFieldVSUnit array = F.cast(field);
                    array.setCollectionType(null);//removes trait
                    array.setType(Integer.TYPE.getName());
                    array.setFieldName("array");
                    return field;
                })
                .withFieldRefactor("compValMap", field -> {//change the collection type
                    VersionedSerialization.MapFieldVSUnit array = F.cast(field);
                    array.setCollectionType(LinkedHashMap.class.getName());
                    return field;
                })
        );

        // modify tree to
        //Data -> Data2
        //Student -> StudentNew
        ser.applyVersionChange(newRoot);
        DLog.print(yaml.dump(newRoot));

        Data2 deserialized = ser.deserializeRoot(newRoot);

        DLog.print();

    }
}
