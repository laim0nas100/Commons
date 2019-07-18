package lt.lb.commons;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.Promise;

/**
 * Recursion avoiding function modeling. Main purpose: write a recursive
 * function. If likely to get stack overflown, use this framework to replace
 * every recursive call with Caller equivalent, without needing to design an
 * iterative solution.
 *
 * Preformance and memory penalties are self-evident.
 *
 * @author laim0nas100
 */
public class Caller<T> {

    private static final List<?> empty = new ArrayList<>(0);
    private boolean hasValue;
    private boolean hasCall;
    private T value;
    private Function<List<T>, Caller<T>> call;
    private List<Caller<T>> dependencies;

    public static class CallerBuilder<T> {

        /**
         * Create new caller
         *
         * @param size expected dependencies (for better performance)
         */
        public CallerBuilder(int size) {
            if (size <= 0) {
                dependants = F.cast(empty);
            } else {
                dependants = new ArrayList<>(size);
            }
        }

        public CallerBuilder() {
            dependants = new ArrayList<>(); // not empty, but default
        }

        private List<Caller<T>> dependants;

        public CallerBuilder<T> withDependency(Caller<T> dep) {
            this.dependants.add(dep);
            return this;
        }

        public CallerBuilder<T> withDependencyCall(Function<List<T>, Caller<T>> call) {
            return this.withDependency(ofFunction(call));
        }

        public CallerBuilder<T> withDependencyResult(Function<List<T>, T> call) {
            return this.withDependency(ofResultCall(call));
        }

        public CallerBuilder<T> withDependencySupp(Supplier<Caller<T>> call) {
            return this.withDependency(ofSupplier(call));
        }

        public CallerBuilder<T> withDependencySuppResult(Supplier<T> call) {
            return this.withDependency(ofSupplierResult(call));
        }

        public CallerBuilder<T> withDependencyResult(T res) {
            return this.withDependency(ofResult(res));
        }

        public Caller<T> toResultCall(Function<List<T>, T> call) {
            return new Caller<>(args -> ofResult(call.apply(args)), this.dependants);
        }

        public Caller<T> toResultCall(Supplier<T> call) {
            return new Caller<>(args -> ofResult(call.get()), this.dependants);
        }

        public Caller<T> toCall(Function<List<T>, Caller<T>> call) {
            return new Caller<>(call, this.dependants);
        }

        public Caller<T> toCall(Supplier<Caller<T>> call) {
            return new Caller<>(args -> call.get(), this.dependants);
        }
    }

    public static class CallerForContinue<T> {

        private final Caller<T> caller;
        private final boolean endCycle;

        private CallerForContinue(Caller<T> caller, boolean endCycle) {
            this.caller = caller;
            this.endCycle = endCycle;
        }

    }

    /**
     * Signify for loop end inside Caller for loop. Equivalent of using return.
     *
     * @param <T>
     * @param next next Caller object
     * @return
     */
    public static <T> CallerForContinue<T> forEnd(Caller<T> next) {
        return new CallerForContinue<>(next, true);
    }

    /**
     * Signify for loop continue inside Caller for loop
     *
     * @param <T>
     * @return
     */
    public static <T> CallerForContinue<T> forContinue() {
        return new CallerForContinue<>(null, false);
    }

    /**
     * Recursive for loop modeling builder.
     *
     * @param <T> the main type of Caller product
     * @param <R> item type that iteration happens
     */
    public static class CallerForBuilder<R, T> {

        private ReadOnlyIterator<R> iter;
        private BiFunction<Integer, R, Caller<T>> contFunction;
        private BiFunction<Integer, T, CallerForContinue<T>> thenFunction;
        private boolean lazy;

        /**
         * @param iterator ReadOnlyIterator that has items
         *
         */
        public CallerForBuilder(ReadOnlyIterator<R> iterator) {
            this.iter = iterator;
        }

        public CallerForBuilder(Stream<R> stream) {
            this(ReadOnlyIterator.of(stream));
        }

        public CallerForBuilder(Iterator<R> iterator) {
            this(ReadOnlyIterator.of(iterator));
        }

        public CallerForBuilder(Collection<R> collection) {
            this(ReadOnlyIterator.of(collection));
        }

        public CallerForBuilder(R... array) {
            this(ReadOnlyIterator.of(array));
        }

        /**
         * Create recursive calls for each (index,item) pair in iterator.
         *
         * @param contFunction
         * @return
         */
        public CallerForEach<R, T> forEachCall(BiFunction<Integer, R, Caller<T>> contFunction) {
            Objects.requireNonNull(contFunction);
            this.contFunction = contFunction;

            return new CallerForEach<>(this);
        }

        /**
         * Create recursive calls for each item in iterator.
         *
         * @param contFunction
         * @return
         */
        public CallerForEach<R, T> forEachCall(Function<R, Caller<T>> contFunction) {
            Objects.requireNonNull(contFunction);
            return this.forEachCall((i, item) -> contFunction.apply(item));
        }

    }

    public static class CallerForEach<R, T> {

        private CallerForBuilder<R, T> callerFor;

        private CallerForEach(CallerForBuilder<R, T> callerFor) {
            this.callerFor = callerFor;
        }

        /**
         * Lazy evaluation. How to evaluate each item ignoring indices
         *
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluateLazy(Function<T, CallerForContinue<T>> thenFunction) {
            Objects.requireNonNull(thenFunction);
            return this.evaluate(true, thenFunction);
        }

        /**
         * Lazy evaluation. How to evaluate each item
         *
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluateLazy(BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
            Objects.requireNonNull(thenFunction);
            return this.evaluate(true, thenFunction);
        }

        /**
         * Eager evaluation. How to evaluate each item ignoring indices
         *
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluateEager(Function<T, CallerForContinue<T>> thenFunction) {
            Objects.requireNonNull(thenFunction);
            return this.evaluate(false, thenFunction);
        }

        /**
         * Eager evaluation. How to evaluate each item
         *
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluateEager(BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
            Objects.requireNonNull(thenFunction);
            return this.evaluate(false, thenFunction);
        }

        /**
         * How to evaluate each item ignoring indices
         *
         * @param lazy evaluation policy
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluate(boolean lazy, Function<T, CallerForContinue<T>> thenFunction) {
            return this.evaluate(lazy, (i, item) -> thenFunction.apply(item));
        }

        /**
         * How to evaluate each item
         *
         * @param lazy evaluation policy
         * @param thenFunction evaluation function that gets how to proceed in
         * the middle of a for loop
         * @return final builder stage
         */
        public CallerForEnd<R, T> evaluate(boolean lazy, BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
            Objects.requireNonNull(thenFunction);
            this.callerFor.lazy = lazy;
            this.callerFor.thenFunction = thenFunction;

            return new CallerForEnd<>(callerFor);
        }

    }

    public static class CallerForEnd<R, T> {

        private CallerForBuilder<R, T> callerFor;

        private CallerForEnd(CallerForBuilder<R, T> callerFor) {
            this.callerFor = callerFor;
        }

        /**
         * @param afterwards Caller when iterator runs out of items (or never
         * had them to begin with) and <b>for</b> loop never exited inside.
         * @return Caller instance of such for loop
         */
        public Caller<T> afterwards(Caller<T> afterwards) {
            Objects.requireNonNull(afterwards);
            if (callerFor.lazy) {
                return Caller.ofIteratorLazy(afterwards, callerFor.iter, callerFor.contFunction, callerFor.thenFunction);
            } else {
                return Caller.ofIteratorChain(afterwards, callerFor.iter, callerFor.contFunction, callerFor.thenFunction);
            }
        }
    }

    /**
     *
     * @param <T>
     * @param result
     * @return Caller, that has a result
     */
    public static <T> Caller<T> ofResult(T result) {
        return new Caller<>(result);
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call
     */
    public static <T> Caller<T> ofFunction(Function<List<T>, Caller<T>> call) {
        Objects.requireNonNull(call);
        return new Caller<>(call);
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call
     */
    public static <T> Caller<T> ofSupplier(Supplier<Caller<T>> call) {
        Objects.requireNonNull(call);
        return new Caller<>(args -> call.get());
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofSupplierResult(Supplier<T> call) {
        Objects.requireNonNull(call);
        return new Caller<>(args -> ofResult(call.get()));
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofResultCall(Function<List<T>, T> call) {
        Objects.requireNonNull(call);
        return new Caller<>(args -> ofResult(call.apply(args)));
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
    private static <T, R> Caller<T> ofIteratorLazy(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, R, Caller<T>> func, BiFunction<Integer, T, CallerForContinue<T>> contFunc) {

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
    private static <T, R> Caller<T> ofIteratorChain(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, R, Caller<T>> func, BiFunction<Integer, T, CallerForContinue<T>> contFunc) {
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

    /**
     * Iteration builder factory method. Prefer calling using <b>new</b>
     * operator for explicit typing.
     *
     * @param <T> item type, that function returns
     * @param <R> item in for loop type
     * @param iter
     * @return
     */
    public static <T, R> CallerForBuilder<R, T> ofIterationBuilder(ReadOnlyIterator<R> iter) {
        return new CallerForBuilder<>(iter);
    }

    /**
     * Just with result
     *
     * @param result
     */
    Caller(T result) {
        this.hasValue = true;
        this.value = result;
        this.dependencies = F.cast(empty);
    }

    /**
     * With recursive tail call
     *
     * @param nextCall
     */
    Caller(Function<List<T>, Caller<T>> nextCall) {
        this.hasCall = true;
        this.call = nextCall;
        this.dependencies = F.cast(empty);
    }

    /**
     * With recursive tail call, which has dependency
     *
     * @param nextCall
     * @param dependency
     */
    Caller(Function<List<T>, Caller<T>> nextCall, Caller<T>... dependency) {
        this(nextCall, Arrays.asList(dependency));
    }

    /**
     * With recursive tail call, which has dependencies
     *
     * @param nextCall
     * @param dependencies
     */
    Caller(Function<List<T>, Caller<T>> nextCall, List<Caller<T>> dependencies) {
        this.hasCall = true;
        this.call = nextCall;
        this.dependencies = dependencies;
    }

    /**
     * Construct Caller for loop end from this caller
     *
     * @return
     */
    public CallerForContinue<T> toForEnd() {
        return Caller.forEnd(this);
    }

    /**
     * Construct CallerBuilder with this caller as first dependency
     *
     * @return
     */
    public CallerBuilder<T> toCallerBuilderAsDep() {
        return new CallerBuilder<T>(1).withDependency(this);
    }

    /**
     * Resolve value without limits
     *
     * @return
     */
    public T resolve() {
        return Caller.resolve(this);
    }
    
    public T resolveThreaded(){
        return Caller.resolveThreaded(this, Optional.empty(), Optional.empty(), 12);
    }

    private static class StackFrame<T> {

        private Caller<T> call;
        private List<T> args;
        private Integer index;

        public StackFrame(Caller<T> call) {
            this.args = new ArrayList<>(0);
            this.call = call;
            this.index = 0;
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
            if (current.getAndIncrement() >= callLimit.get()) {
                throw new CallerException("Call limit reached " + current.get());
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

        Deque<StackFrame<T>> stack = new ArrayDeque<>();
        ArrayList<T> emptyArgs = new ArrayList<>(0);
        AtomicLong callNumber = new AtomicLong(0);

        while (true) {
            if (stack.isEmpty()) {
                if (caller.hasValue) {
                    return caller.value;
                } else if (caller.hasCall) {

                    if (caller.dependencies.isEmpty()) {
                        assertCallLimit(callLimit, callNumber);
                        caller = caller.call.apply(emptyArgs);
                    } else {
                        stack.addLast(new StackFrame(caller));
                    }
                } else {
                    throw new IllegalStateException("No value or call"); // should never happen
                }
            } else { // in stack
                if (stackLimit.isPresent() && stackLimit.get() <= stack.size()) {
                    throw new IllegalStateException("Stack limit overrun " + stack.size());
                }
                StackFrame<T> frame = stack.getLast();
                caller = frame.call;
                if (caller.dependencies.size() <= frame.args.size()) { //demolish stack, because got all dependecies
                    assertCallLimit(callLimit, callNumber);
                    caller = caller.call.apply(frame.args); // last call with dependants
                    if (caller.hasCall) {
                        stack.getLast().clearWith(caller);
                    } else if (caller.hasValue) {
                        stack.pollLast();
                        if (stack.isEmpty()) {
                            return caller.value;
                        } else {
                            stack.getLast().args.add(caller.value);
                        }
                    } else {
                        throw new IllegalStateException("No value or call"); // should never happen
                    }
                } else { // not demolish stack
                    if (caller.hasValue) {
                        frame.args.add(caller.value);
                    } else if (caller.hasCall) {
                        if (caller.dependencies.isEmpty()) { // just call, assume we have expanded stack before
                            assertCallLimit(callLimit, callNumber);
                            frame.clearWith(caller.call.apply(emptyArgs)); // replace current frame, because of simple tail recursion
                        } else { // dep not empty
                            Caller<T> get = caller.dependencies.get(frame.index);
                            frame.index++;
                            if (get.hasValue) {
                                frame.args.add(get.value);
                            } else if (get.hasCall) {
                                stack.addLast(new StackFrame<>(get));
                            } else {
                                throw new IllegalStateException("No value or call"); // should never happen
                            }
                        }
                    }
                }
            }
        }
    }

    public static <T> T resolveThreaded(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit, int branch) {
        ExecutorService service = Executors.newCachedThreadPool();
        try {
            T resolved = resolveThreadedInner(caller, stackLimit, callLimit, branch, 0, new AtomicLong(0), service);
            List<Runnable> shutdownNow = service.shutdownNow();
            if(!shutdownNow.isEmpty()){
                throw new IllegalStateException("No all tasks terminated?");
            }
            return resolved;
        } catch (InterruptedException | ExecutionException ex) {
            throw NestedException.of(ex.getCause());
        }

    }

    private static <T> T resolveThreadedInner(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit, int branch, int prevStackSize, AtomicLong callNumber, Executor exe) throws InterruptedException, ExecutionException {

        Deque<StackFrame<T>> stack = new ArrayDeque<>();
        ArrayList<T> emptyArgs = new ArrayList<>(0);

        while (true) {
            if (stack.isEmpty()) {
                if (caller.hasValue) {
                    return caller.value;
                } else if (caller.hasCall) {

                    if (caller.dependencies.isEmpty()) {
                        assertCallLimit(callLimit, callNumber);
                        caller = caller.call.apply(emptyArgs);
                    } else {
                        stack.addLast(new StackFrame(caller));
                    }
                } else {
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
                    caller = caller.call.apply(frame.args); // last call with dependants
                    if (caller.hasCall) {
                        stack.getLast().clearWith(caller);
                    } else if (caller.hasValue) {
                        stack.pollLast();
                        if (stack.isEmpty()) {
                            return caller.value;
                        } else {
                            stack.getLast().args.add(caller.value);
                        }
                    } else {
                        throw new IllegalStateException("No value or call"); // should never happen
                    }
                } else { // not demolish stack
                    if (caller.hasValue) {
                        frame.args.add(caller.value);
                    } else if (caller.hasCall) {
                        if (caller.dependencies.isEmpty()) { // just call, assume we have expanded stack before
                            assertCallLimit(callLimit, callNumber);
                            frame.clearWith(caller.call.apply(emptyArgs)); // replace current frame, because of simple tail recursion
                        } else { // dep not empty
                            if (branch > 0 && caller.dependencies.size() > 1) {
                                Promise[] array = new Promise[caller.dependencies.size()];
                                int stackSize = stack.size() + prevStackSize;
                                F.iterate(caller.dependencies, (i, c) -> {
                                    if (c.hasValue) {
                                        array[i] = new Promise(() -> c.value);
                                    } else if (c.hasCall) {
                                        array[i] = new Promise(() -> { // actually use recursion, because localizing is hard, and has to be fast, so just limit branching size
                                            return resolveThreadedInner(Caller.ofFunction(c.call), stackLimit, callLimit, branch - 1, stackSize, callNumber, exe);
                                        });
                                    }
                                    array[i].execute(exe);
                                });
                                new Promise<>().waitFor(array).execute(exe).get();
                                for (Promise pro : array) {
                                    frame.args.add((T) pro.get());

                                }
                                frame.index += array.length;
                            } else {
                                Caller<T> get = caller.dependencies.get(frame.index);
                                frame.index++;
                                if (get.hasValue) {
                                    frame.args.add(get.value);
                                } else if (get.hasCall) {
                                    stack.addLast(new StackFrame<>(get));
                                } else {
                                    throw new IllegalStateException("No value or call"); // should never happen
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class CallerException extends IllegalStateException {

        public CallerException() {
        }

        public CallerException(String s) {
            super(s);
        }

        public CallerException(String message, Throwable cause) {
            super(message, cause);
        }

        public CallerException(Throwable cause) {
            super(cause);
        }

    }
}
