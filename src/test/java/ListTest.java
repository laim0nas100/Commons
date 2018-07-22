
import com.google.common.collect.testing.*;
import static com.google.common.collect.testing.features.CollectionFeature.KNOWN_ORDER;
import static com.google.common.collect.testing.features.CollectionFeature.SERIALIZABLE;
import static com.google.common.collect.testing.features.CollectionFeature.SERIALIZABLE_INCLUDING_VIEWS;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.Feature;
import com.google.common.collect.testing.testers.*;
import com.google.common.testing.SerializableTester;
import java.lang.reflect.Method;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import lt.lb.commons.Log;
import org.junit.Ignore;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Lemmin
 */
@Ignore
public class ListTest extends com.google.common.collect.testing.TestsForListsInJavaUtil {

    @Override
    public Test allTests() {
        TestSuite allTests = new TestSuite();
        allTests.addTest(testsForPrefillArray());
        return allTests;
    }

    protected Collection<Method> suppressForPrefillArrayList() {
        return Collections.emptySet();
    }

    public Test testsForPrefillArray() {
        return MyListTestSuiteBuilder.using(new TestStringListGenerator() {
            @Override
            protected List<String> create(String[] strings) {
                LinkedList<String> list = new LinkedList<>();
                for (String s : strings) {
                    list.add(s);
                }
                return list;
            }
        }).named("PrefillArrayList")
                .suppressing(suppressForPrefillArrayList())
                .withFeatures(CollectionSize.ANY).createTestSuite();

    }

    public static class MyListTestSuiteBuilder<E> extends AbstractCollectionTestSuiteBuilder<MyListTestSuiteBuilder<E>, E> {

        public static <E> MyListTestSuiteBuilder<E> using(TestListGenerator<E> generator) {
            return new MyListTestSuiteBuilder<E>().usingGenerator(generator);
        }

        @Override
        protected List<Class<? extends AbstractTester>> getTesters() {
            List<Class<? extends AbstractTester>> testers = Helpers.copyToList(super.getTesters());

            Log.print(testers.remove(CollectionToArrayTester.class));
            testers.add(CollectionSerializationEqualTester.class);
            testers.add(ListAddAllAtIndexTester.class);
            testers.add(ListAddAllTester.class);
            testers.add(ListAddAtIndexTester.class);
            testers.add(ListAddTester.class);
            testers.add(ListCreationTester.class);
            testers.add(ListEqualsTester.class);
            testers.add(ListGetTester.class);
            testers.add(ListHashCodeTester.class);
            testers.add(ListIndexOfTester.class);
            testers.add(ListLastIndexOfTester.class);
            testers.add(ListListIteratorTester.class);
            testers.add(ListRemoveAllTester.class);
            testers.add(ListRemoveAtIndexTester.class);
            testers.add(ListRemoveTester.class);
            testers.add(ListReplaceAllTester.class);
            testers.add(ListRetainAllTester.class);
            testers.add(ListSetTester.class);
            testers.add(ListSubListTester.class);
            testers.add(ListToArrayTester.class);
            return testers;
        }

        /**
         * Specifies {@link CollectionFeature#KNOWN_ORDER} for all list tests, since lists have an
         * iteration ordering corresponding to the insertion order.
         */
        @Override
        public TestSuite createTestSuite() {
            withFeatures(KNOWN_ORDER);
            return super.createTestSuite();
        }

        @Override
        protected List<TestSuite> createDerivedSuites(
                FeatureSpecificTestSuiteBuilder<?, ? extends OneSizeTestContainerGenerator<Collection<E>, E>> parentBuilder) {
            List<TestSuite> derivedSuites = new ArrayList<>(super.createDerivedSuites(parentBuilder));

            if (parentBuilder.getFeatures().contains(SERIALIZABLE)) {
                derivedSuites.add(
                        com.google.common.collect.testing.ListTestSuiteBuilder.using(
                                new ReserializedListGenerator<E>(parentBuilder.getSubjectGenerator()))
                                .named(getName() + " reserialized")
                                .withFeatures(computeReserializedCollectionFeatures(parentBuilder.getFeatures()))
                                .suppressing(parentBuilder.getSuppressedTests())
                                .createTestSuite());
            }
            return derivedSuites;
        }

        class ReserializedListGenerator<E> implements TestListGenerator<E> {

            final OneSizeTestContainerGenerator<Collection<E>, E> gen;

            private ReserializedListGenerator(OneSizeTestContainerGenerator<Collection<E>, E> gen) {
                this.gen = gen;
            }

            @Override
            public SampleElements<E> samples() {
                return gen.samples();
            }

            @Override
            public List<E> create(Object... elements) {
                return (List<E>) SerializableTester.reserialize(gen.create(elements));
            }

            @Override
            public E[] createArray(int length) {
                return gen.createArray(length);
            }

            @Override
            public Iterable<E> order(List<E> insertionOrder) {
                return gen.order(insertionOrder);
            }
        }

        private Set<Feature<?>> computeReserializedCollectionFeatures(Set<Feature<?>> features) {
            Set<Feature<?>> derivedFeatures = new HashSet<>();
            derivedFeatures.addAll(features);
            derivedFeatures.remove(SERIALIZABLE);
            derivedFeatures.remove(SERIALIZABLE_INCLUDING_VIEWS);
            return derivedFeatures;
        }

    }
}
