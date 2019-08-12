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
                return getJob().isDone() || (!getJob().isScheduled() && !getJob().isRunning());
            }
        };
    }

    /**
     * Adds JobDependecy to all jobs, that only allows 1 job to be executing at
     * any given time. Creates (n-1) * (n-1) dependencies, where n is number of
     * jobs submitted.
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
                    main.addDependency(whileNotExecuting(other));
                }
            });
        });
    }

    /**
     * Chain all jobs forward to given root job.
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
     * Chains given jobs consecutively forward
     *
     * @param jobs
     * @param evName
     */
    public static void forwardChain(Collection<Job> jobs, String evName) {
        Job prev = null;
        for (Job job : jobs) {
            if (prev == null) {
                prev = job;
                continue;
            }

            prev.chainForward(evName, job);
            prev = job;
        }
    }

    /**
     * Chains given jobs consecutively backward
     *
     * @param jobs
     * @param evName
     */
    public static void backwardChain(Collection<Job> jobs, String evName) {
        Job prev = null;
        for (Job job : jobs) {
            if (prev == null) {
                prev = job;
                continue;
            }

            prev.chainBackward(evName, job);
            prev = job;
        }
    }

    /**
     * Chain all jobs backward to given root job.
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
     * Resolve root leafs. Can be used to determine jobs without
     * JobDependencies.
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
