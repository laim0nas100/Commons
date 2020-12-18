package regression.threading.jobs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lt.lb.commons.Java;
import lt.lb.commons.Log;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.jobsystem.Dependencies;
import lt.lb.jobsystem.Job;
import lt.lb.jobsystem.JobExecutor;
import lt.lb.jobsystem.ScheduledJobExecutor;
import lt.lb.jobsystem.VoidJob;
import lt.lb.jobsystem.dependency.Dependency;
import lt.lb.jobsystem.dependency.MutuallyExclusivePointCAS;
import lt.lb.jobsystem.dependency.MutuallyExclusivePoint;
import lt.lb.jobsystem.events.SystemJobEventName;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class JobTest {

    public static Job<Void> incrementJob(LongValue val, Integer increments) {

        return new VoidJob(j -> {
            for (int i = 0; i < increments; i++) {
                val.incrementAndGet();
            }
        });

    }

    public static void doIncrement(int jobs, Consumer<ArrayList<Job>> jobDepModifier) throws InterruptedException {
        JobExecutor executor = new JobExecutor(new FastWaitingExecutor(4));
        RandomDistribution rng = RandomDistribution.uniform(new FastRandom());

        ArrayList<Job> jobList = new ArrayList<>();

        ArrayList<Integer> increments = new ArrayList<>(jobs);
        for (int i = 0; i < jobs; i++) {
            increments.add(rng.nextInt(100, 500));
        }

        LongValue val = new LongValue(0L);
        LongValue expected = new LongValue(0L);
        for (Integer inc : increments) {
            jobList.add(incrementJob(val, inc));
            expected.incrementAndGet(inc);
        }

        jobDepModifier.accept(jobList);

        jobList.forEach(executor::submit);

        executor.awaitJobEmptiness(1, TimeUnit.DAYS);
        executor.shutdown();

        assert expected.equals(val);
    }

    @Test
    public void incrementTest() throws InterruptedException {
        doIncrement(100, jobs -> Dependencies.mutuallyExclusive(jobs));
        doIncrement(100, jobs -> Dependencies.backwardChain(jobs, SystemJobEventName.ON_SUCCESSFUL));
        doIncrement(100, jobs -> Dependencies.forwardChain(jobs, SystemJobEventName.ON_SUCCESSFUL));
    }

    public static void addEventLogListeners(Job job) {
        EnumSet<SystemJobEventName> enums = EnumSet.allOf(SystemJobEventName.class);
        enums.forEach(val -> {
            job.addListener(val, e -> {
                Log.print(job.getUUID() + " " + val.name());
            });
        });
    }

    static AtomicLong idGen = new AtomicLong(0L);

    public static String getID() {
        return idGen.getAndIncrement() + "";
    }

    @Test
    public void exclusiveInterestPointTest() throws InterruptedException {
        Log.main().stackTrace = false;

        for (int t = 0; t < 10; t++) {

            JobExecutor executor = new ScheduledJobExecutor(new FastWaitingExecutor(Java.getAvailableProcessors()));
            AtomicLong atomLong = new AtomicLong(0L);
            LongValue longVal1 = new LongValue(0L);
            LongValue longVal2 = new LongValue(0L);
            ThreadLocal<RandomDistribution> rng = ThreadLocal.withInitial(() -> RandomDistribution.uniform(new FastRandom(1337)));

            Integer jobs = rng.get().nextInt(25, 30);
            int middle = rng.get().nextInt(jobs - 20, jobs - 15);

            Integer range = rng.get().nextInt(50000, 100000);

            MutuallyExclusivePoint point1 = new MutuallyExclusivePoint();

            MutuallyExclusivePoint point2 = new MutuallyExclusivePoint();

            Dependency randomDep = j -> {
//            Log.print(j.getUUID() + "-DEP CHECK");
                return true;
            };

            ArrayDeque<Job> allJobs = new ArrayDeque<>();
            for (int i = 0; i < jobs; i++) {
                if (i < middle) {
                    VoidJob jobBoth = new VoidJob(getID() + "B", job -> {
                        for (int j = 0; j < range; j++) {
                            longVal1.incrementAndGet();
                            longVal2.incrementAndGet();
                        }
                    });
                    allJobs.add(jobBoth);
                    jobBoth.addDependency(randomDep);
                    point1.addSharingJob(jobBoth);
                    point2.addSharingJob(jobBoth);

                } else {
                    VoidJob jobEx1 = new VoidJob(getID() + "E1", job -> {
                        for (int j = 0; j < range; j++) {
                            longVal1.incrementAndGet();
                        }
                    });
                    allJobs.add(jobEx1);
                    jobEx1.addDependency(randomDep);
                    point1.addSharingJob(jobEx1);

                    VoidJob jobEx2 = new VoidJob(getID() + "E2", job -> {
                        for (int j = 0; j < range; j++) {
                            longVal2.incrementAndGet();
                        }
                    });
                    allJobs.add(jobEx2);
                    jobEx2.addDependency(randomDep);
                    point2.addSharingJob(jobEx2);

                }

                VoidJob jobSimple = new VoidJob(getID(), job -> {
                    for (int j = 0; j < range; j++) {
                        atomLong.incrementAndGet();
                    }
                });
                jobSimple.addDependency(randomDep);

                allJobs.add(jobSimple);

            }
//            allJobs.forEach(j -> {
//                addEventLogListeners(j);
//            });
executor.submitAll(allJobs);

//        executor.submitAll(allJobs);
            executor.awaitJobEmptiness(1, TimeUnit.DAYS);
            assertThat(atomLong.get()).isEqualTo(longVal1.get()).isEqualTo(longVal2.get());
        }

    }

}
