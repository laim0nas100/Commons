package empiric.core;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lt.lb.commons.ArrayBasedCounter;
import lt.lb.commons.Log;
import lt.lb.commons.io.FileReader;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.F;
import lt.lb.commons.Predicates;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.Memoized;
import lt.lb.commons.parsing.CommentParser;
import lt.lb.commons.threads.FastExecutor;
import org.apache.commons.lang3.concurrent.Computable;
import org.apache.commons.lang3.concurrent.Memoizer;
import org.junit.*;

/**
 *
 * @author laim0nas100
 */
public class CommonsTest {

    public CommonsTest() {
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

    static {
        Log.main().async = true;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    ArrayBasedCounter abc = new ArrayBasedCounter(10, 0);
    BigInteger bigInt = BigInteger.ZERO;

    public void benchCounter() throws InterruptedException {
        long time1, time2;
        time1 = System.currentTimeMillis();

        long times = 10000000;
        long i = 0;
        while (i++ < times) {
            abc.inc(1);

        }

        time1 = System.currentTimeMillis() - time1;
        time2 = System.currentTimeMillis();
        i = 0;
        while (i++ < times) {
            bigInt = bigInt.add(BigInteger.ONE);

        }
        time2 = System.currentTimeMillis() - time2;
        Log.print("Done");

        Log.print(time1, time2);
        Thread.sleep(500);
    }

//    @Test
    public void testFilterDistinct() {
        Collection<Integer> collection = new LinkedList<>(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10));
        Log.print("Initial", collection);
        List<Integer> filterParallel = F.filterParallel(collection, n -> n % 2 == 0, new FastExecutor(4));
        Log.print("Removed after filter", filterParallel);
        Log.print("Left after filter", collection);
        Predicate<Integer> pred = Predicates.filterDistinct(Equator.primitiveHashEquator());
        List<Integer> filterDistinct = collection.stream().parallel().filter(pred).collect(Collectors.toList());

        Log.print("Filtered distinct", filterDistinct);
        F.checkedRun(() -> {
            Log.await(1, TimeUnit.HOURS);
        });

    }

//    @Test
    public void readFile() throws Exception {

        String desktop = "C:\\Users\\Laimonas-Beniusis-PC\\Desktop\\";

        Log.main().async = true;
        Log.main().threadName = false;
        Log.main().timeStamp = false;
        F.unsafeRun(() -> {
            String url = desktop + "myFile.txt";
            ArrayList<String> readFromFile = FileReader.readFromFile(url, "#", "/*", "*/");

            Log.printLines(readFromFile);
            Log.print("########");
        });

        F.unsafeRun(() -> {
            String url = desktop + "myFile.txt";
            ArrayList<String> readFromFile = FileReader.readFromFile(url);
//            Log.printLines(readFromFile);
            ReadOnlyIterator<String> parseAllComments = CommentParser.parseAllComments(ReadOnlyIterator.of(readFromFile), "#", "/*", "*/");
            ArrayList<String> parsed = new ArrayList<>();
            for (String s : parseAllComments) {
//                Log.print(s);
                parsed.add(s);
            }
            Log.print("########");
            Log.printLines(parsed);
        });

        F.unsafeRun(() -> {
            String url = desktop + "myFile2.txt";
            ArrayList<String> readFromFile = FileReader.readFromFile(url, "#", "**", "**");
//            Log.printLines(readFromFile);
            Log.await(1, TimeUnit.HOURS);
        });

    }

    public void parseTest() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("text.txt");
        ArrayList<String> readFromFile = FileReader.readFrom(new FileInputStream(new File(resource.getFile())));
        Log.printLines(readFromFile);
        ReadOnlyIterator<String> parseAllComments = CommentParser.parseAllComments(ReadOnlyIterator.of(readFromFile), "//", "/*", "*/");
        ArrayList<String> parsed = new ArrayList<>();
        for (String s : parseAllComments) {
//                Log.print(s);
            parsed.add(s);
        }
        Log.print("########");
        Log.printLines(parsed);
        Log.close();
    }

//    @Test
    public void convertToArff() {

        Log.main().async = true;
        String desktop = "C:\\Users\\Lemmin\\Desktop\\";
        String relationTitle = "SomeTitle";

        F.unsafeRun(() -> {
            ArrayList<String> readFromFile = FileReader.readFromFile(desktop + "raw.txt");
            int colCount = readFromFile.get(0).split(",").length;
            Log.print(colCount);

            ArrayList<String> arff = new ArrayList<>();
            arff.add("@relation " + relationTitle);
            arff.add("");
            for (int i = 1; i <= colCount; i++) {
                String col = "@attribute col" + i + "   NUMERIC";
                arff.add(col);
            }
            arff.add("");
            arff.add("@data");
            arff.addAll(readFromFile);
            FileReader.writeToFile(desktop + "output.arff", arff);

        });

    }

    public void convertToArffNew() {

        Log.main().async = false;
        String desktop = "C:\\Users\\Lemmin\\Desktop\\";
        String relationTitle = "SomeTitle";

        F.unsafeRun(() -> {
            ArrayList<String> readFromFile = FileReader.readFromFile(desktop + "raw.csv");
            int colCount = readFromFile.get(0).split(",").length;
            Log.print(colCount);

            ArrayList<String> arff = new ArrayList<>();
            arff.add("@relation " + relationTitle);
            arff.add("");
            arff.add("@attribute col2      CLASS");
            arff.add("@attribute col120    NUMERIC");
            arff.add("@attribute col280    NUMERIC");
            arff.add("");
            arff.add("@data");

            for (String line : readFromFile) {
                String[] split = line.split(",");
                String newLine = split[1] + "," + split[119] + "," + split[279];
                arff.add(newLine);

            }
            FileReader.writeToFile(desktop + "outputConverted.arff", arff);

        });

    }

//    @Test
    public void convertToArffNewFinal() {

        Log.main().async = false;
        String desktop = "C:\\Users\\Lemmin\\Desktop\\";
        String relationTitle = "SomeTitle";

        F.unsafeRun(() -> {
            ArrayList<String> readFromFile = FileReader.readFromFile(desktop + "raw.csv");
            int colCount = readFromFile.get(0).split(",").length;
            Log.print(colCount);

            ArrayList<String> arff = new ArrayList<>();
            arff.add("@relation " + relationTitle);
            arff.add("");
            arff.add("@attribute col2      {male,female}");
            arff.add("@attribute col120    {0,1}");
            arff.add("@attribute col280    {sick,healthy}");
            arff.add("");
            arff.add("@data");

            for (String line : readFromFile) {
                String[] split = line.split(",");
                String result = split[279];
                if (!result.trim().equals("1")) {
                    result = "sick";
                } else {
                    result = "healthy";
                }

                String sex = split[1];
                if (sex.equals("0")) {
                    sex = "male";
                } else {
                    sex = "female";
                }
                String newLine = sex + "," + split[119] + "," + result;
                arff.add(newLine);

            }
            FileReader.writeToFile(desktop + "outputConvertedFinal.arff", arff);

        });

    }

    public abstract static class Generator<T> {

        public T prev;

        public abstract T next(T prev);

        public T next() {
            return next(prev);
        }
    }

//    @Test
    public void defaultSort() {
        Generator<Value<Integer>> gen = new Generator<Value<Integer>>() {
            @Override
            public Value<Integer> next(Value<Integer> prev) {
                prev = new Value<>(new Random().nextInt(1000));
                return prev;
            }
        };
        gen.next(new Value<>(0));

        ArrayList<Value<Integer>> vals = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            vals.add(gen.next());
        }

        ExtComparator<Value<Integer>> of = ExtComparator.of((Value<Integer> o1, Value<Integer> o2) -> Integer.compare(o1.get(), o2.get()));

        Collections.sort(vals, of);
        Log.printLines(vals);
        F.unsafeRun(() -> {
            Log.await(1, TimeUnit.HOURS);
        });

    }

//    @Test
    public void memoizerTest() {
        Lambda.L1R<Long, BigInteger> of = Lambda.of(StackOverflowTest.RecursionBuilder::fibb2);

        Memoized memoized = new Memoized();
        Lambda.L1R<Long, BigInteger> memoize = memoized.memoize(of);

        Log.print("RUN");
        Runnable r1 = () -> {
            for (Integer i = 10; i < 35; i++) {
                of.apply(i.longValue());
            }
        };

        Runnable r2 = () -> {
            for (Integer i = 10; i < 35; i++) {
                memoize.apply(i.longValue());
            }
        };

        Benchmark b = new Benchmark();
        b.executeBench(10, "DEF", r1).print(Log::print);
        b.executeBench(10, "MEM", r2).print(Log::print);
        b.executeBench(10, "DEF", r1).print(Log::print);
        b.executeBench(10, "MEM", r2).print(Log::print);

        Log.print("END RUN");

    }
}
