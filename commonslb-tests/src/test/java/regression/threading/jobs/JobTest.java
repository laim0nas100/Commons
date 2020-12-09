package regression.threading.jobs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lt.lb.commons.Java;
import lt.lb.commons.Log;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.jobsystem.Dependencies;
import lt.lb.jobsystem.Job;
import lt.lb.jobsystem.JobExecutor;
import lt.lb.jobsystem.ScheduledJobExecutor;
import lt.lb.jobsystem.VoidJob;
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

//        return new Job<>((Job<Void> j) -> {
//            for (int i = 0; i < increments; i++) {
//                val.incrementAndGet();
//            }
//            
//        });
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

    @Test
    public void exclusiveInterestPointTest() throws InterruptedException {
        JobExecutor executor = new JobExecutor(new FastWaitingExecutor(Java.getAvailableProcessors()));
        AtomicLong atomLong = new AtomicLong(0L);
        LongValue longVal = new LongValue(0L);
        RandomDistribution rng = RandomDistribution.uniform(new FastRandom());

        Integer jobs = rng.nextInt(20, 25);

        Integer range = rng.nextInt(50000, 100000);

        MutuallyExclusivePoint point = new MutuallyExclusivePoint();

        ArrayDeque<Job> allJobs = new ArrayDeque<>();
        for (int i = 0; i < jobs; i++) {
            VoidJob jobExclusive = new VoidJob(job -> {
                for (int j = 0; j < range; j++) {
                    longVal.incrementAndGet();
                }
            });

            point.addSharingJob(jobExclusive);
            jobExclusive.addDependency(j -> Java.getNanoTimePlus() % 10 < 3);
            VoidJob jobSimple = new VoidJob(job -> {
                for (int j = 0; j < range; j++) {
                    atomLong.incrementAndGet();
                }
            });
            jobSimple.addDependency(j -> Java.getNanoTimePlus() % 10 < 3);
            allJobs.add(jobExclusive);
            allJobs.add(jobSimple);
        }

        executor.submitAll(allJobs);

        executor.awaitJobEmptiness(1, TimeUnit.DAYS);
        assertThat(atomLong.get()).isEqualTo(longVal.get());

    }
}
