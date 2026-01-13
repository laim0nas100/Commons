package regression.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lt.lb.commons.DLog;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.io.serialization.VSManager;
import lt.lb.commons.io.serialization.VersionedChanges;
import lt.lb.commons.io.serialization.VersionedDeserializationContext;
import lt.lb.commons.io.serialization.VersionedSerialization;
import lt.lb.commons.iteration.For;
import lt.lb.uncheckedutils.Checked;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializationTest {

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

    public static class SelfRef {

        public String string;
        public SelfRef referenced;
    }


    @Test
    public void testSerializers() throws Exception {
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
        ser.includeCustomRefCounting(Student.class, 0);
        ser.includeCustomRefCounting(Data.class, 0);
        ser.includeCustomRefCounting(StudentNew.class, 1);
        ser.includeCustomRefCounting(Data2.class, 1);
        ser.exludeBase(Runnable.class);
        ser.withStringifyType(BigInteger.class, s -> new BigInteger(s));
        ser.withStringifyType(BigDecimal.class, s -> new BigDecimal(s));
//        ser.withStringifyType(ZonedDateTime.class, s -> ZonedDateTime.parse(s));

        VersionedSerialization.CustomVSU root1 = ser.serializeRoot(data);
        VersionedSerialization.CustomVSU root2 = ser.serializeRoot(data);

        Assertions.assertThat(root1).describedAs("Same object serializeRoot twice").isEqualTo(root2); // should be the same every time
        VersionedSerialization.VSUnit rootClone = root1.uncheckedClone();
        assertThat(root1).describedAs("CustomVSU cloning").isEqualTo(rootClone);

        //read serialized back from bytes
        Object deserializeRoot = ser.deserializeRoot(root1);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ser.serializingObjectStream().objectToStream(deserializeRoot, new ObjectOutputStream(byteArrayOutputStream));
        Object readObject;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            readObject = objectInputStream.readObject();
        }
        assertThat(root1).describedAs("CustomVSU reading from object input stream").isEqualTo(readObject);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ser.serializingXMLStream().objectToStream(deserializeRoot, new BufferedOutputStream(bytes));
        Object streamToObject = ser.serializingXMLStream().streamToObject(new BufferedInputStream(new ByteArrayInputStream(bytes.toByteArray()))).get();
        VersionedSerialization.CustomVSU xmlDeserializedRoot = ser.serializeRoot(streamToObject);
        assertThat(root1).describedAs("CustomVSU serialized and deserialized from xml").isEqualTo(xmlDeserializedRoot);

    }

    @Test
    public void testVersionChanges() throws Exception {
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
        ser.includeCustomRefCounting(Student.class, 0);
        ser.includeCustomRefCounting(Data.class, 0);
        ser.includeCustomRefCounting(StudentNew.class, 1);
        ser.includeCustomRefCounting(Data2.class, 1);
        ser.exludeBase(Runnable.class);
        ser.withStringifyType(BigInteger.class, s -> new BigInteger(s));
        ser.withStringifyType(BigDecimal.class, s -> new BigDecimal(s));
        ser.withStringifyType(ZonedDateTime.class, s -> ZonedDateTime.parse(s));

        ser.addVersionChanger(VersionedChanges.builderVerionInc(Student.class, 0)
                .withFieldRename("lName", "lastName")
                .withTypeChange(StudentNew.class)
        );
        ser.addVersionChanger(VersionedChanges.builderVerionInc(Data.class, 0)
                .withTypeChange(Data2.class)
                .withFieldRefactor("numbers", field -> {//change the List<Integer>  to int[] and  fieldName
                    VersionedSerialization.ArrayVSUF array = F.cast(field);
                    array.setCollectionType(null);//removes trait
                    array.setType(Integer.TYPE.getName());
                    array.setFieldName("array");
                    return array;
                })
                .withFieldRefactor("compValMap", field -> {//change the collection type
                    VersionedSerialization.MapVSUF array = F.cast(field);
                    array.setCollectionType(LinkedHashMap.class.getName());
                    return array;
                })
        );

        VersionedSerialization.CustomVSU oldData = ser.serializeRoot(data);
        ser.applyVersionChange(oldData);
        assertThat(oldData.getVersion()).describedAs("Version is changed").isEqualTo(1L);
        assertThat(oldData.getType()).describedAs("Tyhpe is changed").isEqualTo(Data2.class.getName());
        Data2 newData = ser.deserializeRoot(oldData);

        //verions change view serializing streams
        Checked.uncheckedRun(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ser.<Data>serializingObjectStream().objectToStream(data, new ObjectOutputStream(byteArrayOutputStream));
            Data2 readObject = ser.<Data2>serializingObjectStream().streamToObject(new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))).get();
        });

        Checked.uncheckedRun(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ser.<Data>serializingXMLStream().objectToStream(data, new BufferedOutputStream(byteArrayOutputStream));
            Data2 readObject = ser.<Data2>serializingXMLStream().streamToObject(new BufferedInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))).get();
        });

    }


}
