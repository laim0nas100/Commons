package lt.lb.commons;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lt.lb.commons.containers.tuples.Tuple;

/**
 *
 * @author laim0nas100
 */
public class Caller<T> {

    private boolean hasValue;
    private boolean hasCall;
    private T value;
    private Function<List<T>, Caller<T>> call;
    private List<Caller<T>> dependants = new ArrayList<>();

    public static class CallerBuilder<T> {

        private List<Caller<T>> dependants = new ArrayList<>();

        public static <T> Caller<T> ofResult(T result) {
            return new Caller<>(result);
        }

        public static <T> Caller<T> ofFunction(Function<List<T>, Caller<T>> call) {
            return new Caller<>(call);
        }

        public static <T> Caller<T> ofSupplier(Supplier<Caller<T>> call) {
            return new Caller<>(args -> call.get());
        }

        public static <T> Caller<T> ofSupplierResult(Supplier<T> call) {
            return new Caller<>(args -> ofResult(call.get()));
        }

        public static <T> Caller<T> ofResultCall(Function<List<T>, T> call) {
            return new Caller<>(args -> ofResult(call.apply(args)));
        }

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
        
        public CallerBuilder<T> withDependencyResult(T res){
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
        
        public Caller<T> toCall(Supplier<Caller<T>> call){
            return new Caller<>(args -> call.get(), this.dependants);
        }
    }

    /**
     * Just with result
     *
     * @param result
     */
    public Caller(T result) {
        this.hasValue = true;
        this.value = result;
    }

    /**
     * With recursive tail call
     *
     * @param nextCall
     */
    public Caller(Function<List<T>, Caller<T>> nextCall) {
        this.hasCall = true;
        this.call = nextCall;
    }

    /**
     * With recursive tail call, which has dependency
     *
     * @param nextCall
     * @param dependency
     */
    public Caller(Function<List<T>, Caller<T>> nextCall, Caller<T>... dependency) {
        this(nextCall, Arrays.asList(dependency));
    }

    /**
     * With recursive tail call, which has dependencies
     *
     * @param nextCall
     * @param dependencies
     */
    public Caller(Function<List<T>, Caller<T>> nextCall, List<Caller<T>> dependencies) {
        this(new Tuple<>(nextCall, dependencies.stream().collect(Collectors.toList())));
    }

    private Caller(Tuple<Function<List<T>, Caller<T>>, List<Caller<T>>> info) {
        this.hasCall = true;
        this.call = info.g1;
        this.dependants = info.g2;
    }

    public T resolve() {
        return Caller.resolve(this);
    }


    private static class StackFrame<T> {

        private Caller<T> call;
        private List<T> args;
        private Integer index;

        public StackFrame(Caller<T> call) {
            this.args = new LinkedList<>();
            this.call = call;
            this.index = 0;
        }
        
        public void clearWith(Caller<T> call){
            this.args.clear();
            this.call = call;
            this.index = 0;
        }
    }

    public static <T> T resolve(Caller<T> caller) {

        ArrayDeque<StackFrame<T>> stack = new ArrayDeque<>();
        ArrayList<T> empty = new ArrayList<>(0);

        while (true) {
            if (stack.isEmpty()) {
                if (caller.hasValue) {
                    return caller.value;
                } else if (caller.hasCall) {

                    if (caller.dependants.isEmpty()) {
                        caller = caller.call.apply(empty);
                    } else {
                        stack.addLast(new StackFrame(caller));
                    }
                } else {
                    throw new IllegalStateException("No value or call");
                }
            } else { // in stack
                StackFrame<T> frame = stack.getLast();
                caller = frame.call;
                if (caller.dependants.size() <= frame.args.size()) { //demolish stack, because got all dependecies
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
                        throw new IllegalStateException("No value or call");
                    }
                } else { // not demolish stack
                    if (caller.hasValue) {
                        frame.args.add(caller.value);
                    } else if (caller.hasCall) {
                        if (caller.dependants.isEmpty()) { // just call, assume we have expanded stack before
                            frame.clearWith(caller.call.apply(empty)); // replace current frame, because of simple tail recursion
                        } else { // dep not empty
                            Caller<T> get = caller.dependants.get(frame.index);
                            frame.index++;
                            if (get.hasValue) {
                                frame.args.add(get.value);
                            } else if (get.hasCall) {
                                stack.addLast(new StackFrame<>(get));
                            } else {
                                throw new IllegalStateException("No value or call");
                            }
                        }
                    }
                }
            }
        }
    }
}
