package regression.jobs;

import java.util.ArrayList;
import java.util.function.Consumer;
import lt.lb.commons.containers.LongValue;
import lt.lb.commons.datafill.NumberFill;
import lt.lb.commons.jobs.Job;
import lt.lb.commons.jobs.JobEvent;
import lt.lb.commons.jobs.JobExecutor;
import lt.lb.commons.jobs.Jobs;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.threads.FastWaitingExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author laim0nas100
 */
public class JobTest {
    
    
    public static Job incrementJob(LongValue val, Integer increments){
        return new Job(()->{
            for(int i = 0; i < increments; i++){
                val.incrementAndGet();
            }
            return null;
        });
    }
    
    public static void doIncrement(int jobs, Consumer<ArrayList<Job>> jobDepModifier) throws InterruptedException{
        JobExecutor executor = new JobExecutor(new FastWaitingExecutor(4));
        RandomDistribution rng = RandomDistribution.uniform(new FastRandom());
        
        ArrayList<Job> jobList = new ArrayList<>();
        ArrayList<Integer> increments = NumberFill.fillArrayList(jobs, rng.getIntegerSupplier(100, 500));
        
        LongValue val = new LongValue(0L);
        LongValue expected = new LongValue(0L);
        for(Integer inc:increments){
            jobList.add(incrementJob(val,inc));
            expected.incrementAndGet(inc);
        }
        
        jobDepModifier.accept(jobList);
        
        for(Job job:jobList){
            executor.submit(job);
        }
        
        executor.shutdown();
        
        
        assert executor.awaitTermination(WaitTime.ofHours(1));
        assertThat(expected).isEqualTo(val);
    }
    
    
   
    
    @Test
    public void incrementTest() throws InterruptedException{
        doIncrement(10, jobs -> Jobs.mutuallyExclusive(jobs));
        doIncrement(10, jobs -> Jobs.backwardChain(jobs, JobEvent.ON_SUCCEEDED));
        doIncrement(10, jobs -> Jobs.forwardChain(jobs, JobEvent.ON_SUCCEEDED));
    }
}
