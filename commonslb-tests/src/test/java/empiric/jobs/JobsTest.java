package empiric.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Java;
import lt.lb.commons.Log;
import lt.lb.commons.jobs.Dependencies;
import lt.lb.commons.jobs.Jobs;
import lt.lb.commons.jobs.Job;
import lt.lb.commons.jobs.JobEvent;
import lt.lb.commons.jobs.JobExecutor;
import lt.lb.commons.jobs.SystemJobEventName;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class JobsTest {

    static RandomDistribution rng = RandomDistribution.uniform(new Random());

    public static Job makeJob(String txt, List<Job> jobs) {
        Job<Number> job = new Job<>(txt, j -> {
            Log.print("In execute", txt);
            Long nextLong = rng.nextLong(1000L, 2000L);
            Thread.sleep(nextLong);
//            if (rng.nextInt(10) >= 8) {
//                throw new RuntimeException("OOPSIE");
//            }
            return nextLong;

        });
        job.addListener(SystemJobEventName.ON_FAILED_TO_START, e -> {
            Log.print("Failed to start ", txt);
        });

        job.addListener(SystemJobEventName.ON_EXECUTE, e -> {
            Log.print("Execute ", txt);
        });

        job.addListener(SystemJobEventName.ON_CANCEL, e -> {
            Log.print("Cancel ", txt);
        });

        job.addListener(SystemJobEventName.ON_DONE, e -> {
            Log.print("Done", txt);
        });
        job.addListener(SystemJobEventName.ON_SUCCESSFUL, e -> {
            Log.print("Success", txt);
        });
        job.addListener(SystemJobEventName.ON_FAILED, e -> {
            Log.print("Failed, cancelling", txt);
            e.getCreator().cancel();
        });

        job.addListener(SystemJobEventName.ON_SCHEDULED, e -> {
            Log.print("Scheduled", txt);
        });

        job.addListener(SystemJobEventName.ON_DISCARDED, e -> {
            Log.print("Discarded", txt);
        });

        job.addDependency(() -> new Random().nextBoolean());
        jobs.add(job);
        return job;
    }

    public static void main(String... args) throws Exception {
        Log.main().async = false;
        ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
        FastExecutor executor = new FastExecutor(8);
        JobExecutor exe = new JobExecutor(5, executor, executor);
        List<Job> jobs = new ArrayList<>();

        Job j0 = makeJob("0", jobs);
        Job j1 = makeJob("1", jobs);
        Job j2 = makeJob("2", jobs);
        Job j3 = makeJob("3", jobs);
        Job j4 = makeJob("4", jobs);
        Job j5 = makeJob("5", jobs);
        Job j6 = makeJob("6", jobs);
        Job j7 = makeJob("7", jobs);

        j0.chainForward(j1).chainForward(j2).chainForward(j3);
        j1.chainForward(j4).chainForward(j5);
        j2.chainForward(j6);
        j3.chainForward(j7);

        sched.scheduleAtFixedRate(() -> {
            System.out.print("Scheduler: ");
            exe.rescanJobs();

        }, 0, 1, TimeUnit.SECONDS);

//        Job f = makeJob("Final", new ArrayList<>());
//        Jobs.chainBackward(f, JobEvent.ON_DONE, Jobs.resolveChildLeafs(j0));
//        Dependencies.mutuallyExclusive(jobs);
        jobs.forEach(exe::submit);

        exe.shutdown();
        exe.awaitTermination(WaitTime.ofDays(1));
        sched.shutdown();
        Log.close();

    }
}
