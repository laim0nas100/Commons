package lt.lb.commons.caller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.F;
import static lt.lb.commons.caller.Caller.CallerType.FUNCTION;
import static lt.lb.commons.caller.Caller.CallerType.RESULT;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.threads.Promise;

/**
 *
 * @author laim0nas100
 */
public class CallerImpl {

    private static class StackFrame<T> {

        private Caller<T> call;
        private List<T> args;
        private List<T> curriedArgs = new LinkedList<>();
        private Optional<StackFrame<T>> parent;
        private Integer index;

        public StackFrame(StackFrame<T> parent, Caller<T> call) {
            this.args = new ArrayList<>(call.dependencies.size());
            this.call = call;
            this.index = 0;
            this.parent = Optional.ofNullable(parent);
        }

        public StackFrame(Caller<T> call) {
            this(null, call);
        }

        public void clearWith(Caller<T> call) {
            this.args.clear();
            this.call = call;
            this.index = 0;
        }
    }

    /**
     * Resolve given caller without limits
     *
     * @param <T>
     * @param caller
     * @return
     */
    public static <T> T resolve(Caller<T> caller) {
        return resolve(caller, Optional.empty(), Optional.empty());
    }

    private static void assertCallLimit(Optional<Long> callLimit, AtomicLong current) {
        if (callLimit.isPresent()) {
            long lim = current.getAndIncrement();
            if (lim >= callLimit.get()) {
                throw new CallerException("Call limit reached " + lim);
            }
        }
    }

    /**
     * Resolve function call chain with optional limits
     *
     * @param <T>
     * @param caller
     * @param stackLimit limit of a stack size (each nested dependency expands
     * stack by 1). Use Optional.empty to disable limit.
     * @param callLimit limit of how many calls can be made (useful for endless
     * recursion detection). Use Optional.empty to disable limit.
     * @return
     */
    public static <T> T resolve(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit) {

        return resolveThreaded(caller, stackLimit, callLimit, -1, Runnable::run); // should never throw exceptions related to threading

    }

    /**
     * Resolve function call chain with optional limits
     *
     * @param <T>
     * @param caller
     * @param stackLimit limit of a stack size (each nested dependency expands
     * stack by 1). Use Optional.empty to disable limit.
     * @param callLimit limit of how many calls can be made (useful for endless
     * recursion detection). Use Optional.empty to disable limit.
     * @param branch how many branch levels to allow (uses recursion) amount of
     * forks is determined by {@code Caller} dependencies
     * @param exe executor
     * @return
     */
    public static <T> T resolveThreaded(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit, int branch, Executor exe) {
        try {
            T resolved = resolveThreadedInner(caller, stackLimit, callLimit, branch, 0, new AtomicLong(0), exe);
            return resolved;
        } catch (InterruptedException | ExecutionException ex) {
            throw NestedException.of(ex);
        }

    }

    /**
     * Retrieves items one by one, each time creating new call. Doesn't make
     * call chain for every item in iterator. Chain version is usually faster if
     * item count returned by iterator is low (up to 3500)
     *
     * Chain version creates call chain, and then evaluates it all, while lazy
     * version creates call chain as needed.
     *
     * @param <T> the main type of Caller product
     * @param <R> type that iteration happens
     * @param emptyCase Caller when iterator is empty of not terminated anywhere
     * @param iterator ReadOnlyIterator that has items
     * @param func BiFunction that provides Caller that eventually results in T
     * type of variable. Used to make recursive calls from all items.
     * @param contFunc BiFunction that checks wether to end iteration in the
     * middle of it and how
     * @return
     */
    public static <T, R> Caller<T> ofIteratorLazy(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, R, Caller<T>> func, BiFunction<Integer, T, CallerForContinue<T>> contFunc) {

        if (!iterator.hasNext()) {
            return emptyCase;
        }
        R next = iterator.getNext();
        Integer index = iterator.getCurrentIndex();

        return new CallerBuilder<T>(1)
                .withDependencyCall(args -> func.apply(index, next))
                .toCall(args -> {

                    CallerForContinue<T> apply = contFunc.apply(index, args.get(0));
                    if (apply.endCycle) {
                        return apply.caller;
                    } else {
                        return ofIteratorLazy(emptyCase, iterator, func, contFunc);
                    }
                });

    }

    /**
     * Retrieves items all at once. Creates dependent call chain. Lazy version
     * is usually faster if iterator returns many items (more than 3500) or item
     * retrieval is not free.
     *
     * Chain version creates call chain, and then evaluates it all, while lazy
     * version creates call chain as needed.
     *
     * @param <T> the main type of Caller product
     * @param <R> type that iteration happens
     * @param emptyCase Caller when iterator is empty of not terminated anywhere
     * @param iterator ReadOnlyIterator that has items
     * @param func BiFunction that provides Caller that eventually results in T
     * type of variable. Used to make recursive calls from all items.
     * @param contFunc BiFunction that checks wether to end iteration in the
     * middle of it and how
     * @return
     */
    public static <T, R> Caller<T> ofIteratorChain(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, R, Caller<T>> func, BiFunction<Integer, T, CallerForContinue<T>> contFunc) {
        int i = 0;
        Caller<T> call = null;
        for (R c : iterator) {
            final int index = i;
            final Caller<T> cont = Caller.ofFunction(args -> {
                return func.apply(index, c);
            });
            if (i == 0) { // first
                call = cont;
            } else {
                call = new CallerBuilder<T>(1)
                        .withDependency(call)
                        .toCall(args -> {
                            CallerForContinue<T> apply = contFunc.apply(index, args.get(0));
                            if (apply.endCycle) {
                                return apply.caller;
                            } else {
                                return cont;
                            }
                        });
            }
            i++;

        }
        return F.nullWrap(call, emptyCase);
    }

    private static <T> List<T> merge(List<T>... lists) {
        return Stream.of(lists).flatMap(m -> m.stream()).collect(Collectors.toList());
    }

    private static <T> T resolveThreadedInner(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit, int branch, int prevStackSize, AtomicLong callNumber, Executor exe) throws InterruptedException, ExecutionException {

        Deque<StackFrame<T>> stack = new ArrayDeque<>();
        ArrayList<T> emptyArgs = new ArrayList<>(0);

        while (true) {
            if (stack.isEmpty()) {
                switch (caller.type) {
                    case RESULT:
                        return caller.value;
                    case FUNCTION:
                        if (caller.dependencies.isEmpty()) {
                            assertCallLimit(callLimit, callNumber);
                            caller = caller.call.apply(emptyArgs);
                        } else {
                            stack.addLast(new StackFrame(caller));
                        }
                        break;
                    default:
                        throw new IllegalStateException("No value or call"); // should never happen
                }
            } else { // in stack
                if (stackLimit.isPresent() && (prevStackSize + stackLimit.get()) <= stack.size()) {
                    throw new IllegalStateException("Stack limit overrun " + stack.size() + prevStackSize);
                }
                StackFrame<T> frame = stack.getLast();
                caller = frame.call;
                if (caller.dependencies.size() <= frame.args.size()) { //demolish stack, because got all dependecies
                    assertCallLimit(callLimit, callNumber);
                    List<T> merge = merge(frame.curriedArgs, frame.args);
                    boolean curried = caller.curriedDependencies;
                    caller = caller.call.apply(merge); // last call with dependants

                    switch (caller.type) {
                        case FUNCTION:
                            stack.getLast().clearWith(caller);
                            stack.getLast().parent.filter(p -> p.call.curriedDependencies).ifPresent(parent -> {
                                parent.curriedArgs.addAll(merge);
                            });
                            break;

                        case RESULT:
                            StackFrame<T> pollLast = stack.pollLast();
                            if (stack.isEmpty()) {
                                return caller.value;
                            } else {
                                stack.getLast().args.add(caller.value);
                            }
                            pollLast.parent.filter(p -> p.call.curriedDependencies).ifPresent(parent -> {
                                parent.curriedArgs.addAll(merge);
                            });
                            break;

                        default:
                            throw new IllegalStateException("No value or call"); // should never happen
                    }
                } else { // not demolish stack
                    if (caller.type == RESULT) {
                        frame.args.add(caller.value);
                    } else if (caller.type == FUNCTION) {
                        if (caller.dependencies.isEmpty()) { // just call, assume we have expanded stack before
                            assertCallLimit(callLimit, callNumber);
                            frame.clearWith(caller.call.apply(emptyArgs)); // replace current frame, because of simple tail recursion
                        } else { // dep not empty

                            if (branch <= 0 || caller.dependencies.size() <= 1) {
                                Caller<T> get = caller.dependencies.get(frame.index);
                                frame.index++;
                                switch (get.type) {
                                    case RESULT:
                                        frame.args.add(get.value);
                                        break;
                                    case FUNCTION:
                                        stack.addLast(new StackFrame<>(frame, get));
                                        break;
                                    default:
                                        throw new IllegalStateException("No value or call"); // should never happen
                                }
                            } else { // use threading with dependencies 
                                Promise[] array = new Promise[caller.dependencies.size()];
                                int stackSize = stack.size() + prevStackSize;
                                F.iterate(caller.dependencies, (i, c) -> {
                                    switch (c.type) {
                                        case RESULT:
                                            array[i] = new Promise(() -> c.value).execute(r -> r.run());
                                            break;
                                        case FUNCTION:
                                            array[i] = new Promise(() -> { // actually use recursion, because localizing is hard, and has to be fast, so just limit branching size
                                                return resolveThreadedInner(c, stackLimit, callLimit, branch - 1, stackSize, callNumber, exe);
                                            }).execute(exe);
                                            break;
                                        default:
                                            throw new IllegalStateException("No value or call"); // should never happen
                                    }
                                });
                                Promise<Object> waiterAndRunner = new Promise<>(Arrays.asList(array));

                                try {
                                    waiterAndRunner.run(); // help with progress
                                    waiterAndRunner.get(); // wait for execution
                                } catch (ExecutionException err) {
                                    while (err.getCause() instanceof ExecutionException) {
                                        err = (ExecutionException) err.getCause();
                                    }
                                    throw err;

                                }
                                for (Promise pro : array) {
                                    frame.args.add((T) pro.get());
                                }
                                frame.index += array.length;
                            }
                        }
                    }
                }
            }
        }
    }
}
