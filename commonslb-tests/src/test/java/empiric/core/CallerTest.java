package empiric.core;

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
public class CallerTest<T> {

    private boolean hasValue;
    private boolean hasCall;
    private T value;
    private Function<List<T>, CallerTest<T>> call;
    private List<CallerTest<T>> dependants = new ArrayList<>();

    public static class CallerBuilder<T> {

        private List<CallerTest<T>> dependants = new ArrayList<>();

        public static <T> CallerTest<T> ofResult(T result) {
            return new CallerTest<>(result);
        }

        public static <T> CallerTest<T> ofFunction(Function<List<T>, CallerTest<T>> call) {
            return new CallerTest<>(call);
        }

        public static <T> CallerTest<T> ofSupplier(Supplier<CallerTest<T>> call) {
            return new CallerTest<>(args -> call.get());
        }

        public static <T> CallerTest<T> ofSupplierResult(Supplier<T> call) {
            return new CallerTest<>(args -> ofResult(call.get()));
        }

        public static <T> CallerTest<T> ofResultCall(Function<List<T>, T> call) {
            return new CallerTest<>(args -> ofResult(call.apply(args)));
        }

        public CallerBuilder<T> withDependency(CallerTest<T> dep) {
            this.dependants.add(dep);
            return this;
        }

        public CallerBuilder<T> withDependencyCall(Function<List<T>, CallerTest<T>> call) {
            return this.withDependency(ofFunction(call));
        }

        public CallerBuilder<T> withDependencyResult(Function<List<T>, T> call) {
            return this.withDependency(ofResultCall(call));
        }

        public CallerBuilder<T> withDependencySupp(Supplier<CallerTest<T>> call) {
            return this.withDependency(ofSupplier(call));
        }

        public CallerBuilder<T> withDependencySuppResult(Supplier<T> call) {
            return this.withDependency(ofSupplierResult(call));
        }
        
        public CallerBuilder<T> withDependencyResult(T res){
            return this.withDependency(ofResult(res));
        }

        public CallerTest<T> toResultCall(Function<List<T>, T> call) {
            return new CallerTest<>(args -> ofResult(call.apply(args)), this.dependants);
        }

        public CallerTest<T> toCall(Function<List<T>, CallerTest<T>> call) {
            return new CallerTest<>(call, this.dependants);
        }
    }

    /**
     * Just with result
     *
     * @param result
     */
    public CallerTest(T result) {
        this.hasValue = true;
        this.value = result;
    }

    /**
     * With recursive tail call
     *
     * @param nextCall
     */
    public CallerTest(Function<List<T>, CallerTest<T>> nextCall) {
        this.hasCall = true;
        this.call = nextCall;
    }

    /**
     * With recursive tail call, which has dependency
     *
     * @param nextCall
     * @param dependency
     */
    public CallerTest(Function<List<T>, CallerTest<T>> nextCall, CallerTest<T>... dependency) {
        this(nextCall, Arrays.asList(dependency));
    }

    /**
     * With recursive tail call, which has dependencies
     *
     * @param nextCall
     * @param dependencies
     */
    public CallerTest(Function<List<T>, CallerTest<T>> nextCall, List<CallerTest<T>> dependencies) {
        this(new Tuple<>(nextCall, dependencies.stream().collect(Collectors.toList())));
    }

    private CallerTest(Tuple<Function<List<T>, CallerTest<T>>, List<CallerTest<T>>> info) {
        this.hasCall = true;
        this.call = info.g1;
        this.dependants = info.g2;
    }

    public T resolve() {
        return CallerTest.resolve(this);
    }


    private static class StackFrame<T> {

        private CallerTest<T> call;
        private List<T> args;
        private Integer index;

        public StackFrame(CallerTest<T> call) {
            this.args = new LinkedList<>();
            this.call = call;
            this.index = 0;
        }
        
        public void clearWith(CallerTest<T> call){
            this.args.clear();
            this.call = call;
            this.index = 0;
        }
    }

    public static <T> T resolve(CallerTest<T> caller) {

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
                    } else{
                        throw new IllegalStateException("No value or call");
                    }
                } else { // not demolish stack
                    if (caller.hasValue) {
                        frame.args.add(caller.value);
                    } else if (caller.hasCall) {
                        if (caller.dependants.isEmpty()) { // just call, assume we have expanded stack before
                            frame.clearWith(caller.call.apply(empty)); // replace current frame, because of simple tail recursion
                        } else { // dep not empty
                            CallerTest<T> get = caller.dependants.get(frame.index);
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
