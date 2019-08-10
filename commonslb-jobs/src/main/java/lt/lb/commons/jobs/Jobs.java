package lt.lb.commons.jobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public class Jobs {

    /**
     * Creates job dependency based on standard events defined in JobEvent.
     *
     * @param job
     * @param event
     * @return
     */
    public static DefaultJobDependency standard(Job job, String event) {
        return new DefaultJobDependency(job, event);
    }

    /**
     * Creates JobDependency, which is satisfied while provided job is not
     * executing.
     *
     * @param job
     * @return
     */
    public static <T> JobDependency<T> whileNotExecuting(Job<T> job) {
        return new JobDependency<T>() {
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
     * Adds JobDependecy to all jobs, that only allows 1 job to be executing at
     * any given time.
     *
     * @param jobs
     */
    public static void mutuallyExclusive(Collection<Job> jobs) {
        if (jobs.size() <= 1) { // nothing to exclude
            return;
        }

        F.iterate(jobs, (i, main) -> {
            F.iterate(jobs, (j, other) -> {
                if (!Objects.equals(i, j)) {
                    main.addDependencyBefore(whileNotExecuting(other));
                }
            });
        });
    }

    /**
     * Chain all jobs forward.
     *
     * @param root
     * @param evName
     * @param deps
     */
    public static void chainForward(Job root, String evName, Collection<Job> deps) {
        for (Job j : deps) {
            root.chainForward(evName, j);
        }
    }

    /**
     * Chain all jobs backward.
     *
     * @param root
     * @param evName
     * @param deps
     */
    public static void chainBackward(Job root, String evName, Collection<Job> deps) {
        for (Job j : deps) {
            root.chainBackward(evName, j);
        }
    }

    public static Dependency any(JobDependency... deps) {
        if (ArrayOp.isEmpty(deps)) {
            throw new IllegalArgumentException("JobDependecies are empty");
        }

        return () -> ArrayOp.any(j -> j.isCompleted(), deps);
    }

    public static Dependency all(JobDependency... deps) {
        if (ArrayOp.isEmpty(deps)) {
            throw new IllegalArgumentException("JobDependecies are empty");
        }

        return () -> ArrayOp.all(j -> j.isCompleted(), deps);
    }

    /**
     * Resolve child leafs. Can be used to determine jobs without anything after
     * them.
     *
     * @param root
     * @return
     */
    public static List<Job> resolveChildLeafs(Job root) {
        ArrayList<Job> leafs = new ArrayList<>();
        Consumer<Job> cons = job -> {
            if (job.doAfter.isEmpty()) {
                leafs.add(job);
            }
        };

        TreeVisitor<Job> visitor = TreeVisitor.ofAll(cons, node -> ReadOnlyIterator.of(node.doAfter));
        visitor.BFS(root);
        return leafs;
    }

    /**
     * Resolve root leafs. Can be used to determine jobs without dependencies.
     *
     * @param root
     * @return
     */
    public static List<Job> resolveRootLeafs(Job root) {
        ArrayList<Job> leafs = new ArrayList<>();
        Consumer<Job> cons = job -> {
            if (job.doBefore.isEmpty()) {
                leafs.add(job);
            }
        };

        TreeVisitor<Job> visitor = TreeVisitor.ofAll(cons, node -> {
            Collection<Dependency> doBefore = node.doBefore;
            return ReadOnlyIterator.of(doBefore.stream().filter(p -> p instanceof JobDependency))
                    .map(m -> (JobDependency) m).map(m -> m.getJob());
        });
        visitor.BFS(root);
        return leafs;
    }

}
