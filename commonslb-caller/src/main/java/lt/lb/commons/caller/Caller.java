package lt.lb.commons.caller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;

/**
 * Recursion avoiding function modeling.Main purpose: write a recursive
 * function. If likely to get stack overflown, use this framework to replace
 * every recursive call with Caller equivalent, without needing to design an
 * iterative solution.
 *
 * Performance and memory penalties are self-evident. Is not likely to be faster
 * than well-made iterative solution.
 *
 * @author laim0nas100
 * @param <T> Most common type of arguments that this is caller is used to model.
 */
public class Caller<T> {

    static enum CallerType {
        RESULT, FUNCTION, SHARED
    }
    
    private static final List<?> empty = new ArrayList<>(0);
    protected final CallerType type;
    protected T value;
    protected String tag;
    protected Function<CastList<T>, Caller<T>> call;
    protected List<Caller<T>> dependencies;
    
    
    
    /**
     * Shared thins
     */
    protected CompletableFuture<T> compl;
    protected AtomicReference<Thread> runner;
    
    /**
     * Signify {@code for} loop end inside {@code Caller for} loop.
     * Equivalent of using {@code return} with recursive function call.
     *
     * @param <T>
     * @param next next Caller object
     * @return
     */
    public static <T> CallerFlowControl<T> flowReturn(Caller<T> next) {
        return new CallerFlowControl<>(next, CallerFlowControl.CallerForType.RETURN);
    }
    
    /**
     * Signify {@code for} loop end inside {@code Caller for} loop.
     * Equivalent of using {@code break}.
     *
     * @param <T>
     * @return
     */
    public static <T> CallerFlowControl<T> flowBreak() {
        return new CallerFlowControl<>(null, CallerFlowControl.CallerForType.BREAK);
    }

    /**
     * Signify {@code for} loop continue inside {@code Caller for} loop.
     *
     * @param <T>
     * @return
     */
    public static <T> CallerFlowControl<T> flowContinue() {
        return new CallerFlowControl<>(null, CallerFlowControl.CallerForType.CONTINUE);
    }

    /**
     *
     * @param <T>
     * @param result
     * @return Caller, that has a result
     */
    public static <T> Caller<T> ofResult(T result) {
        return new Caller<>(CallerType.RESULT, result, null, F.cast(empty));
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call
     */
    public static <T> Caller<T> ofFunction(Function<CastList<T>, Caller<T>> call) {
        Objects.requireNonNull(call);
        return new Caller<>(CallerType.FUNCTION, null, call, F.cast(empty));
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call
     */
    public static <T> Caller<T> ofSupplier(Supplier<Caller<T>> call) {
        Objects.requireNonNull(call);
        return ofFunction(args -> call.get());
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofSupplierResult(Supplier<T> call) {
        Objects.requireNonNull(call);
        return ofFunction(args -> ofResult(call.get()));
    }

    /**
     *
     * @param <T>
     * @param call
     * @return Caller, with recursive tail call, that ends up as a result
     */
    public static <T> Caller<T> ofResultCall(Function<CastList<T>, T> call) {
        Objects.requireNonNull(call);
        return ofFunction(args -> ofResult(call.apply(args)));
    }

    /**
     * Main constructor
     *
     * @param nextCall
     */
    Caller(CallerType type, T result, Function<CastList<T>, Caller<T>> nextCall, List<Caller<T>> dependencies) {
        this.type = type;
        this.value = result;
        this.call = nextCall;
        this.dependencies = dependencies;
        if(type == CallerType.SHARED){
            this.compl = new CompletableFuture<>();
            this.runner = new AtomicReference<>();
        }
    }

    /**
     * Construct Caller loop end with {@code return} from this caller
     *
     * @return
     */
    public CallerFlowControl<T> toFlowReturn() {
        return Caller.flowReturn(this);
    }

    /**
     * Tag of this caller. Default is null. For debugging or saving caller
     * instances.
     *
     * @return tag of this caller
     */
    public String getTag() {
        return tag;
    }

    /**
     * Replace tag of this caller
     *
     * @param tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Replace tab of this caller with a builder pattern
     *
     * @param tag
     * @return
     */
    public Caller<T> withTag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Construct CallerBuilder with this caller as first dependency
     *
     * @return
     */
    public CallerBuilder<T> toCallerBuilderAsDep() {
        return new CallerBuilder<T>(1).with(this);
    }

    /**
     * Construct SharedCallerBuilder with this caller as first dependency
     *
     * @return
     */
    public CallerBuilder<T> toSharedCallerBuilderAsDep() {
        return new SharedCallerBuilder<T>(1).with(this);
    }

    /**
     * Resolve value without limits
     *
     * @return
     */
    public T resolve() {
        return Caller.resolve(this);
    }

    public T resolveThreaded() {
        return Caller.resolveThreaded(this, Optional.empty(), Optional.empty(), 10, ForkJoinPool.commonPool());
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
        return CallerImpl.resolveThreaded(caller, stackLimit, callLimit, -1, Runnable::run); // should never throw exceptions related to threading

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
        return CallerImpl.resolveThreaded(caller, stackLimit, callLimit, branch, exe);
    }
}
