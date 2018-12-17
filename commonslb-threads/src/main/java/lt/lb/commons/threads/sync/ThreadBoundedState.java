package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.Predicates;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.misc.Range;

/**
 * Combines AtomicInteger to manage how many threads enter each state
 *
 * @author laim0nas100
 */
public class ThreadBoundedState {

    private static class StateInfo {

        String name;
        int threadBound;
        AtomicInteger threadCount = new AtomicInteger(0);
    }

    private volatile StateInfo[] states;

    /**
     *
     * @param stateCount number of states starting from 0
     * @param maxThreads number of threads. if less or equal to 0, then
     * unbounded
     */
    public ThreadBoundedState(int stateCount, int maxThreads) {
        states = new StateInfo[stateCount];
        F.iterate(states, (i, NULL) -> {
            states[i] = new StateInfo();
            states[i].name = "State " + i;
            states[i].threadBound = maxThreads;
        });
    }

    /**
     * Give a name to a state
     *
     * @param alias
     * @param stateIndex
     */
    public void setAlias(String alias, int stateIndex) {

        //unique check
        int count = (int) Stream.of(states).filter(Predicates.filterDistinct(Equator.valueEquator(st -> st.name))).count();
        if (alias == null || count != states.length) {
            throw new IllegalArgumentException("Illegal name for state" + alias);
        }
        states[stateIndex].name = alias;
    }

    /**
     * set thread bound of a state
     *
     * @param stateIndex
     * @param bound
     *
     */
    public void setThreadBound(int stateIndex, int bound) {
        states[stateIndex].threadBound = bound;
    }

    private int nameToIndex(String stateName) {
        Optional<Integer> find = nameToStateIndex(stateName);
        if (!find.isPresent()) {
            throw new IllegalArgumentException("State with name " + stateName + " was not found");
        } else {
            return find.get();
        }
    }

    /**
     * Get state index by name
     *
     * @param stateName
     * @return
     */
    public Optional<Integer> nameToStateIndex(String stateName) {
        return F.find(states, (i, st) -> Objects.equals(st.name, stateName)).map(t -> t.g1);
    }

    /**
     * Enter a state by name
     *
     * @param stateName
     * @return
     */
    public boolean enter(String stateName) {
        return enter(this.nameToIndex(stateName));
    }

    /**
     * Exit a state by name
     *
     * @param stateName
     * @return
     */
    public boolean exit(String stateName) {
        return exit(this.nameToIndex(stateName));
    }

    /**
     * Transition states by name
     *
     * @param from
     * @param to
     * @return
     */
    public boolean transition(String from, String to) {
        return transition(nameToIndex(from), nameToIndex(to));
    }

    /**
     * Enter a state
     *
     * @param state
     * @return
     */
    public boolean enter(int state) {
        StateInfo st = states[state];
        if (st.threadBound <= 0) {
            st.threadCount.incrementAndGet();
            return true;
        }
        if (st.threadCount.incrementAndGet() > st.threadBound) {
            st.threadCount.decrementAndGet();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Exit a state
     *
     * @param state
     * @return
     */
    public boolean exit(int state) {
        StateInfo st = states[state];
        if (st.threadBound <= 0) {
            st.threadCount.decrementAndGet();
            return true;
        }

        if (st.threadCount.decrementAndGet() < 0) {
            st.threadCount.incrementAndGet();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Transition states
     *
     * @param from
     * @param to
     * @return
     */
    public boolean transition(int from, int to) {
        Range<Integer> range = Range.of(0, states.length);
        if ((!range.inRangeIncExc(from) || !range.inRangeIncExc(to)) || from == to) {
            throw new IllegalArgumentException("Expected different arguments in range " + range.toString()+" got "+from+" "+to);
        }
        StateInfo stateFrom = states[from];
        StateInfo stateTo = states[to];

        boolean success = false;

        boolean noBound1 = stateTo.threadBound <= 0;
        boolean noBound2 = stateFrom.threadBound <= 0;

        if (!noBound1) {
            if (stateTo.threadCount.incrementAndGet() > stateTo.threadBound) {
                stateTo.threadCount.decrementAndGet();

            } else {
                success = true;
            }

        } else { // we can't fail
            stateTo.threadCount.incrementAndGet();
            success = true;
        }
        if (!success) { // nothing to revert just yet
            return false;
        }

        //successfully modified stateTo
        if (!noBound2) {
            if (stateFrom.threadCount.decrementAndGet() < 0) {
                stateFrom.threadCount.incrementAndGet();
                // failed, revert
                stateTo.threadCount.decrementAndGet();
                success = false;
            } else {
                success = true;
            }
        } else {
            stateFrom.threadCount.decrementAndGet();
            success = true;
        }

        return success;

    }

    /**
     * Get threads in given state currently
     *
     * @param state
     * @return
     */
    public int getStateThreadCount(int state) {
        return states[state].threadCount.get();
    }

    /**
     * Get threads in given state currently by name
     *
     * @param name
     * @return
     */
    public int getStateThreadCount(String name) {
        return getStateThreadCount(nameToIndex(name));
    }

}
