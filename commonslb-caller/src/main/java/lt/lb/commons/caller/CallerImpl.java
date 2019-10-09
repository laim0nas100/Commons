package lt.lb.commons.caller;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import lt.lb.commons.F;
import static lt.lb.commons.caller.Caller.CallerType.FUNCTION;
import static lt.lb.commons.caller.Caller.CallerType.RESULT;
import static lt.lb.commons.caller.Caller.CallerType.SHARED;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.threads.Promise;

/**
 *
 * @author laim0nas100
 */
public class CallerImpl {

    private static class StackFrame<T> implements Serializable {

        private Caller<T> call;
        private ArrayList<T> args;
        private Integer index;
        private Deque<Caller<T>> sharedStack;

        public StackFrame(Caller<T> call) {
            clearWith(call);
        }

        public final void clearWith(Caller<T> call) {
            if (call.dependencies == null) {
                this.args = null;
            } else {
                this.args = new ArrayList<>(call.dependencies.size());
            }
            this.call = call;
            this.index = 0;
            if (call.type == SHARED) {
                if (sharedStack == null) {
                    sharedStack = new ArrayDeque<>();
                }
                sharedStack.add(call);
            }
        }

        public boolean readyArgs(Caller<T> c) {
            if (args == null || c.dependencies == null) {
                return true;
            }
            return this.args.size() == c.dependencies.size();
        }

        @Override
        public String toString() {
            return "StackFrame{" + "call=" + call + ", args=" + args + ", index=" + index + ", sharedStack=" + sharedStack + '}';
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
            return resolveThreadedInner(caller, stackLimit, callLimit, branch, 0, new AtomicLong(0), exe);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
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
                call = new CallerBuilder<T>(1).with(call)
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

    private static <T> T complete(Collection<Caller<T>> s, T value) {
        if (s == null) {
            return value;
        }
        for (Caller<T> call : s) {
            if (call.type != SHARED) {
                throw new IllegalStateException("Non shared in complete");
            }
            call.compl.complete(value);
        }
        s.clear();
        return value;
    }

    private static <T> boolean runnerCAS(Caller<T> caller) {
        return caller.runner.compareAndSet(null, Thread.currentThread());
//                || caller.runner.compareAndSet(Thread.currentThread(), Thread.currentThread());
    }
    private static final CastList emptyArgs = new CastList<>(null);

    private static <T> T resolveThreadedInner(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit, int branch, int prevStackSize, AtomicLong callNumber, Executor exe) throws InterruptedException, ExecutionException, TimeoutException {

        Deque<StackFrame<T>> stack = new ArrayDeque<>();

        Deque<Caller<T>> emptyStackShared = new ArrayDeque<>();

        while (true) {
            if (stack.isEmpty()) {
                switch (caller.type) {
                    case RESULT:
                        return complete(emptyStackShared, caller.value);
                    case SHARED:
                        if (caller.compl.isDone()) {
                            return complete(emptyStackShared, caller.compl.get());
                        }
                        if (runnerCAS(caller)) {
                            if (caller.dependencies == null) {
                                assertCallLimit(callLimit, callNumber);
                                emptyStackShared.add(caller);
                                caller = caller.call.apply(emptyArgs);
                            } else {
                                stack.addLast(new StackFrame<>(caller));
                            }
                        } else {
                            return complete(emptyStackShared, caller.compl.get());
                        }

                        break;
                    case FUNCTION:
                        if (caller.dependencies == null) {
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
                if (frame.readyArgs(caller)) { //demolish stack, because got all dependecies
                    assertCallLimit(callLimit, callNumber);
                    caller = caller.call.apply(frame.args == null ? emptyArgs : new CastList(frame.args)); // last call with dependants
                    switch (caller.type) {
                        case SHARED:
                            if (!caller.compl.isDone() && runnerCAS(caller)) {
                                stack.getLast().clearWith(caller);
                            } else { // done or executing on other thread
                                T v = caller.compl.get();
                                complete(stack.getLast().sharedStack, v);
                                stack.pollLast();
                                if (stack.isEmpty()) {
                                    return complete(emptyStackShared, v);
                                } else {
                                    stack.getLast().args.add(v);
                                }
                            }

                            break;
                        case FUNCTION:
                            stack.getLast().clearWith(caller);
                            break;

                        case RESULT:
                            complete(stack.pollLast().sharedStack, caller.value);
                            if (stack.isEmpty()) {
                                return complete(emptyStackShared, caller.value);
                            } else {
                                stack.getLast().args.add(caller.value);
                            }
                            break;

                        default:
                            throw new IllegalStateException("No value or call"); // should never happen
                    }
                } else { // not demolish stack
                    if (caller.type == RESULT) {
                        frame.args.add(caller.value);
                    } else if (caller.type == SHARED && caller.compl.isDone()) {
                        frame.args.add(caller.compl.get());
                    } else if (caller.type == FUNCTION
                            || (caller.type == SHARED && !caller.compl.isDone())) {

                        if (caller.dependencies == null) {

                            // just call, assume we have expanded stack before
                            if (caller.type == FUNCTION || (caller.type == SHARED && runnerCAS(caller))) {
                                assertCallLimit(callLimit, callNumber);
                                frame.clearWith(caller.call.apply(emptyArgs)); // replace current frame, because of simple tail recursion
                            } else {//in another thread

                                frame.args.add(caller.compl.get());
                            }

                        } else { // dep not empty

                            if (branch <= 0 || caller.dependencies.size() <= 1) {
                                Caller<T> get = caller.dependencies.get(frame.index);
                                frame.index++;
                                switch (get.type) {
                                    case RESULT:
                                        frame.args.add(get.value);
                                        break;
                                    case FUNCTION:
                                        stack.addLast(new StackFrame<>(get));
                                        break;
                                    case SHARED:
                                        if (get.compl.isDone()) {
                                            frame.args.add(get.compl.get());
                                        } else {
                                            if (runnerCAS(get)) {
                                                stack.addLast(new StackFrame<>(get));
                                            } else {//in another thread
                                                frame.args.add(get.compl.get());
                                            }

                                        }
                                        break;
                                    default:
                                        throw new IllegalStateException("Unknown caller state" + get);
                                }
                            } else { // use threading with dependencies 
                                ArrayList<Promise<T>> array = new ArrayList<>(caller.dependencies.size());
                                int stackSize = stack.size() + prevStackSize;
                                for (Caller<T> c : caller.dependencies) {
                                    switch (c.type) {
                                        case RESULT:
                                            new Promise<>(() -> c.value).collect(array).run();
                                            break;
                                        case FUNCTION:
                                            new Promise<>(() -> { // actually use recursion, because localizing is hard, and has to be fast, so just limit branching size
                                                return resolveThreadedInner(c, stackLimit, callLimit, branch - 1, stackSize, callNumber, exe);
                                            }).execute(exe).collect(array);
                                            break;
                                        case SHARED:
                                            if (c.compl.isDone()) {
                                                new Promise(c.compl).collect(array).run();
                                            } else {
                                                new Promise(() -> { // actually use recursion, because localizing is hard, and has to be fast, so just limit branching size
                                                    return resolveThreadedInner(c, stackLimit, callLimit, branch - 1, stackSize, callNumber, exe);
                                                }).execute(exe).collect(array);
                                            }
                                            break;
                                        default:
                                            throw new IllegalStateException("Unknown caller state" + c);
                                    }
                                }
                                Promise waiterAndRunner = new Promise(array);

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
                                frame.index += array.size();
                            }
                        }
                    } else { // allready finished executing in another thread
                        frame.args.add(caller.compl.get());
                    }
                }
            }
        }
    }
}
