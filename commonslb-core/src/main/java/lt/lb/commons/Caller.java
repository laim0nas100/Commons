package lt.lb.commons;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 * Recursion avoiding function modeling.
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
        return new Caller<>(call);
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call
     */
    public static <T> Caller<T> ofSupplier(Supplier<Caller<T>> call) {
        return new Caller<>(args -> call.get());
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofSupplierResult(Supplier<T> call) {
        return new Caller<>(args -> ofResult(call.get()));
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofResultCall(Function<List<T>, T> call) {
        return new Caller<>(args -> ofResult(call.apply(args)));
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
     * @param endFunction BiFunction that checks wether to end iteration in the
     * middle of it
     * @param contFunc BiFunction that provides Caller that can continue
     * function calls. If endFunction is satisfied with any given item, the
     * resulting Caller is just the item on which iteration terminates
     * @return
     */
    public static <T, R> Caller<T> ofIteratorChain(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, T, Boolean> endFunction, BiFunction<Integer, R, Caller<T>> contFunc) {
        int i = 0;
        Caller<T> call = null;
        for (R c : iterator) {
            final int index = i;
            final Caller<T> cont = Caller.ofFunction(args -> {
                return contFunc.apply(index, c);
            });
            if (i == 0) { // first
                call = cont;
            } else {
                call = new CallerBuilder<T>(1)
                        .withDependency(call)
                        .toCall(args -> {
                            T result = args.get(0);
                            if (endFunction.apply(index, result)) {
                                return ofResult(result);
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
     * @param endFunction BiFunction that checks wether to end iteration in the
     * middle of it
     * @param contFunc BiFunction that provides Caller that can continue
     * function calls. If endFunction is satisfied with any given item, the
     * resulting Caller is just the item on which iteration terminates
     * @return
     */
    public static <T, R> Caller<T> ofIteratorLazy(Caller<T> emptyCase, ReadOnlyIterator<R> iterator, BiFunction<Integer, T, Boolean> endFunction, BiFunction<Integer, R, Caller<T>> contFunc) {

        if (!iterator.hasNext()) {
            return emptyCase;
        }
        return new CallerBuilder<T>(1)
                .withDependencyCall(args -> contFunc.apply(iterator.getCurrentIndex() + 1, iterator.next()))
                .toCall(args -> {
                    T result = args.get(0);
                    if (endFunction.apply(iterator.getCurrentIndex(), result)) {
                        return ofResult(result);
                    } else {
                        return ofIteratorLazy(emptyCase, iterator, endFunction, contFunc);
                    }
                });
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

    public T resolve() {
        return Caller.resolve(this);
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

    public static <T> T resolve(Caller<T> caller) {
        return resolve(caller, Optional.empty(), Optional.empty());
    }

    /**
     * Resolve function call chain with optional limits
     * @param <T>
     * @param caller
     * @param stackLimit
     * @param callLimit
     * @return 
     */
    public static <T> T resolve(Caller<T> caller, Optional<Integer> stackLimit, Optional<Long> callLimit) {

        ArrayDeque<StackFrame<T>> stack = new ArrayDeque<>();
        ArrayList<T> emptyArgs = new ArrayList<>(0);
        Long callNumber = 0L;

        while (true) {
            if (stack.isEmpty()) {
                if (caller.hasValue) {
                    return caller.value;
                } else if (caller.hasCall) {

                    if (caller.dependencies.isEmpty()) {
                        if (callLimit.isPresent() && callNumber++ >= callLimit.get()) {
                            throw new IllegalStateException("Call limit reached " + callNumber);
                        }
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
                    if (callLimit.isPresent() && callNumber++ >= callLimit.get()) {
                        throw new CallerException("Call limit reached " + callNumber);
                    }
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
                            if (callLimit.isPresent() && callNumber++ >= callLimit.get()) {
                                throw new CallerException("Call limit reached " + callNumber);
                            }
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
