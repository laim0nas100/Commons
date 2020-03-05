package empiric.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.jobs.Jobs;
import lt.lb.commons.jobs.Job;
import lt.lb.commons.jobs.JobEvent;
import lt.lb.commons.jobs.JobExecutor;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class JobsTest {

    RandomDistribution rng = RandomDistribution.uniform(new Random());

    public Job makeJob(String txt, List<Job> jobs) {
        Job<Long> job = new Job<>(txt, j -> {
            Log.print("In execute", txt);
            Long nextLong = rng.nextLong(1000L, 2000L);
            Thread.sleep(nextLong);
            

//            if (rng.nextInt(10) >= 8) {
//                throw new RuntimeException("OOPSIE");
//            }
            return nextLong;

        });
        job.addListener(JobEvent.ON_EXECUTE, e -> {
            Log.print("Execute begin", txt);
        });

        job.addListener(JobEvent.ON_CANCEL, e -> {
            Log.print("Cancel ", txt);
        });

        job.addListener(JobEvent.ON_DONE, e -> {
            Log.print("Done", txt);
        });
        job.addListener(JobEvent.ON_SUCCEEDED, e -> {
            Log.print("Success", txt);
        });
        job.addListener(JobEvent.ON_FAILED, e -> {
            Log.print("Failed, cancelling", txt);
            e.getCreator().cancel();
        });

        job.addListener(JobEvent.ON_SCHEDULED, e -> {
            Log.print("Scheduled", txt);
        });
        jobs.add(job);
        return job;
    }

    public void jobTest() throws Exception {
        JobExecutor exe = new JobExecutor(new FastExecutor(4));
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

//        Job f = makeJob("Final", new ArrayList<>());

//        Jobs.chainBackward(f, JobEvent.ON_DONE, Jobs.resolveChildLeafs(j0));

//        Jobs.mutuallyExclusive(jobs);

        jobs.forEach(job -> exe.submit(job));

        exe.shutdown();
        exe.awaitTermination(WaitTime.ofDays(1));
        Log.await(1, TimeUnit.DAYS);

    }
}
