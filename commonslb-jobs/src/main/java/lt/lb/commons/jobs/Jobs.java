package lt.lb.commons.jobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public class Jobs {

    public static DefaultJobDependency standard(Job job, String event) {
        return new DefaultJobDependency(job, event);
    }

    /**
     * Creates JobDependency, which is satisfied while provided job is not executing.
     * @param job
     * @return 
     */
    public static JobDependency whileNotExecuting(Job job) {
        return new JobDependency() {
            @Override
            public Job getJob() {
                return job;
            }

            @Override
            public boolean isCompleted() {
                return (!getJob().isScheduled() && !getJob().isRunning()) || getJob().isDone();
            }
        };
    }

    /**
     * Adds JobDependecy to all jobs, that only allows 1 job to be executing at any given time.
     * @param jobs 
     */
    public static void mutuallyExclusive(Collection<Job> jobs) {
        if (jobs.size() <= 1) { // nothing to exclude
            return;
        }

        F.iterate(jobs, (i,main)->{
            F.iterate(jobs, (j,other)->{
                if(!Objects.equals(i, j)){
                    main.addDependencyBefore(whileNotExecuting(other));
                }
            });
        });
    }

    public static void addForward(Job root, String evName, Collection<Job> deps) {
        for (Job j : deps) {
            root.addForward(evName, j);
        }
    }

    public static void addBackward(Job root, String evName, Collection<Job> deps) {
        for (Job j : deps) {
            root.addBackward(evName, j);
        }
    }

    public static List<Job> resolveChildLeafs(Job root) {
        ArrayList<Job> leafs = new ArrayList<>();
        Consumer<Job> cons = job -> {
            if (job.doAfter.isEmpty()) {
                leafs.add(job);
            }
        };

        TreeVisitor<Job> visitor = TreeVisitor.ofAll(cons, node -> ReadOnlyIterator.of((Collection<JobDependency>) node.doAfter).map(j -> j.getJob()));
        visitor.BFS(root);
        return leafs;
    }

    public static List<Job> resolveRootLeafs(Job root) {
        ArrayList<Job> leafs = new ArrayList<>();
        Consumer<Job> cons = job -> {
            if (job.doBefore.isEmpty()) {
                leafs.add(job);
            }
        };

        TreeVisitor<Job> visitor = TreeVisitor.ofAll(cons, node -> ReadOnlyIterator.of((Collection<JobDependency>) node.doBefore).map(j -> j.getJob()));
        visitor.BFS(root);
        return leafs;
    }
    
    
}
