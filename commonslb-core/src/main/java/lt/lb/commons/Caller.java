package lt.lb.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.containers.IntegerValue;
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
    public Caller(Function<List<T>, Caller<T>> nextCall, Caller<T> dependency) {
        this(nextCall, Arrays.asList(dependency));
    }

    /**
     * With recursive tail call, which has dependencies
     *
     * @param nextCall
     * @param dependency
     * @param rest
     */
    public Caller(Function<List<T>, Caller<T>> nextCall, Caller<T> dependency, Caller<T>... rest) {
        this(new Tuple<>(nextCall, Stream.concat(Stream.of(dependency), Stream.of(rest)).collect(Collectors.toList())));
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
    
    public T resolve(){
        return Caller.resolve(this);
    }

    public static <T> T resolve(Caller<T> caller) {
        LinkedList<Caller<T>> stack = new LinkedList<>();
        LinkedList<List<T>> stackArgs = new LinkedList<>();
        LinkedList<IntegerValue> stackIndex = new LinkedList<>();

        while (true) {
            if (stack.isEmpty()) {
                if (caller.hasValue) {
                    return caller.value;
                } else if (caller.hasCall) {
                    if (caller.dependants.isEmpty()) {
                        caller = caller.call.apply(new ArrayList<>());
                    } else {
                        stack.addLast(caller);
                        stackArgs.addLast(new ArrayList<>());
                        stackIndex.addLast(new IntegerValue(0));
                    }
                } else {
                    throw new IllegalStateException("No value or call");
                }
            } else { // in stack
                caller = stack.getLast();
                List<T> args = stackArgs.getLast();
                IntegerValue index = stackIndex.getLast();
                if (caller.dependants.size() <= args.size()) { //demolish stack, because got all dependecies
                    stack.pollLast();
                    stackIndex.pollLast();
                    stackArgs.pollLast();

                    caller = caller.call.apply(args); // last call with dependants
                    if (caller.hasCall) {
                        stack.addLast(caller);
                        stackArgs.addLast(new ArrayList<>());
                        stackIndex.addLast(new IntegerValue(0));
                    } else if (caller.hasValue) {
                        if (stack.isEmpty()) {
                            return caller.value;
                        } else {
                            stackArgs.getLast().add(caller.value);
                        }
                    }
                } else { // not demolish stack
                    if (caller.hasValue) {
                        args.add(caller.value);
                    } else if (caller.hasCall) {
                        if (caller.dependants.isEmpty()) { // just call, assume we have expanded stack before
                            stack.pollLast();
                            stackIndex.pollLast();
                            stackArgs.pollLast();
                            caller = caller.call.apply(new ArrayList<>()); // we have to shrink stack
                            stack.addLast(caller);
                            stackArgs.addLast(new ArrayList<>());
                            stackIndex.addLast(new IntegerValue(0));
                        } else { // dep not empty
                            Caller<T> get = caller.dependants.get(index.getAndIncrement());
                            if (get.hasValue) {
                                args.add(get.value);
                            } else if (get.hasCall) {

                                stack.addLast(get);
                                stackArgs.addLast(new ArrayList<>());
                                stackIndex.addLast(new IntegerValue(0));
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
