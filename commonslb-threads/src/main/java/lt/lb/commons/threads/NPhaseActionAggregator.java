package lt.lb.commons.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.misc.numbers.Atomic;

/**
 *
 * @author laim0nas100
 */
public abstract class NPhaseActionAggregator<K, A> implements Runnable {

    protected final AtomicInteger qNumber = new AtomicInteger(0);
    protected final AtomicInteger waiting = new AtomicInteger(0);
    protected final AtomicInteger submitted = new AtomicInteger(0);

    protected final int phaseCount;
    protected final Map<K, AtomicReference<A>>[] maps;

    public NPhaseActionAggregator(int phaseCount) {
        if (phaseCount < 2) {
            throw new IllegalArgumentException("Phase count must be at least 2");
        }
        this.phaseCount = phaseCount;
        maps = new Map[phaseCount];
        for (int i = 0; i < phaseCount; i++) {
            maps[i] = create(i);
        }
    }

    protected Map<K, AtomicReference<A>> getMapAndPhaseSwitch() {
        return maps[qNumber.getAndAccumulate(phaseCount, (prev, pCount) -> (prev + 1) % pCount)]; // priority change
    }

    protected Map<K, AtomicReference<A>> getMap() {
        return maps[qNumber.get()];
    }

    public void addAction(K key, A action) {

        getMap().computeIfAbsent(key, c -> new AtomicReference<>()).set(action);
        Atomic.incrementAndGet(waiting);
        submit();
    }

    public void submit() {
        int get = waiting.get();
        if (get > 0 && submitted.compareAndSet(0, get)) {
            submitLogic();
        }
    }

    protected void phaseExecution() {
        int got = submitted.getAndSet(0);
        if (got == 0) {
            return;
        }
        try {
            Map<K, AtomicReference<A>> qToAdd = getMapAndPhaseSwitch();
            List<K> keys = new ArrayList<>(qToAdd.size());
            qToAdd.entrySet().stream().peek(entry -> {
                keys.add(entry.getKey());
            }).map(e -> e.getValue()).map(m -> m.getAndSet(null)).filter(Objects::nonNull).forEach(this::actionLogic);
            cleanup(qToAdd, keys);
        } finally {

            waiting.addAndGet(-got);//priority execution
        }
    }

    protected abstract void actionLogic(A action);

    protected abstract void submitLogic();

    protected void cleanup(Map<K, AtomicReference<A>> map, Collection<K> presentKeys) {

    }

    protected Map<K, AtomicReference<A>> create(int i) {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        phaseExecution();
    }

}
