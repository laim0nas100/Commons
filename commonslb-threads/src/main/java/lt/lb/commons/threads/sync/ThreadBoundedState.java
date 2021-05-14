package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.Equator;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.streams.StreamMapper.StreamDecorator;
import lt.lb.commons.iteration.streams.StreamMapperEnder;
import lt.lb.commons.iteration.streams.StreamMappers;
import lt.lb.commons.misc.Range;

/**
 * Combines AtomicInteger to manage how many threads enter each state
 *
 * @author laim0nas100
 */
public class ThreadBoundedState {

    private static class StateInfo {

        String name;
        volatile int threadBound;
        AtomicInteger threadCount = new AtomicInteger(0);

        boolean tryEnter() {
            if (threadBound < 0) { // unlimited
                threadCount.incrementAndGet();
                return true;
            }
            if (threadCount.incrementAndGet() > threadBound) {
                threadCount.decrementAndGet();
                return false;
            } else {
                return true;
            }
        }

        boolean tryExit() {
            if (threadBound < 0) {
                threadCount.decrementAndGet();
                return true;
            }

            if (threadCount.decrementAndGet() < 0) {
                threadCount.incrementAndGet();
                return false;
            } else {
                return true;
            }
        }

    }

    private volatile StateInfo[] states;

    /**
     *
     * @param stateCount number of states starting from 0
     * @param maxThreads number of threads. if less than 0, then unbounded
     */
    public ThreadBoundedState(int stateCount, int maxThreads) {
        states = new StateInfo[stateCount];
        For.elements().iterate(states, (i, NULL) -> {
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
        int count = stateInfoMapper.startingWith(states).intValue();
        if (alias == null || count != states.length) {
            throw new IllegalArgumentException("Illegal name for state" + alias);
        }
        states[stateIndex].name = alias;
    }
    private static final StreamMapperEnder<StateInfo, StateInfo, Long> stateInfoMapper
            = new StreamDecorator<StateInfo>()
                    .apply(StreamMappers.distinct(Equator.valueEquator(st -> st.name)))
                    .count();

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
        return For.elements().find(states, (i, st) -> Objects.equals(st.name, stateName)).map(t -> t.index);
    }

    /**
     * Enter a state by name
     *
     * @param stateName
     * @return
     */
    public boolean enter(String stateName) {
        return enter(nameToIndex(stateName));
    }

    /**
     * Exit a state by name
     *
     * @param stateName
     * @return
     */
    public boolean exit(String stateName) {
        return exit(nameToIndex(stateName));
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
        return st.tryEnter();
    }

    /**
     * Exit a state
     *
     * @param state
     * @return
     */
    public boolean exit(int state) {
        StateInfo st = states[state];
        return st.tryExit();
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
            throw new IllegalArgumentException("Expected different arguments in range " + range.toString() + " got " + from + " " + to);
        }
        StateInfo stateFrom = states[from];
        StateInfo stateTo = states[to];

        if (!stateTo.tryEnter()) {
            return false;
        }

        //entered stateTo, now to exit stateFrom
        if (stateFrom.tryExit()) {
            return true;
        } else {
            //clean up stateTo
            stateTo.threadCount.decrementAndGet();
            return false;
        }
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
