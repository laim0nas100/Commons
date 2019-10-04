package lt.lb.commons.caller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.caller.Caller.CallerType;
import static lt.lb.commons.caller.Caller.ofFunction;
import static lt.lb.commons.caller.Caller.ofResult;
import static lt.lb.commons.caller.Caller.ofResultCall;
import static lt.lb.commons.caller.Caller.ofSupplier;
import static lt.lb.commons.caller.Caller.ofSupplierResult;

/**
 *
 * @author laim0nas100
 */
public class CallerBuilder<T> {

    /**
     * Create new caller
     *
     * @param size expected dependencies (for better performance)
     */
    public CallerBuilder(int size) {
        dependants = new ArrayList<>(size);
    }
    
    public CallerBuilder() {
        dependants = new ArrayList<>(); // not empty, but default
    }
    
    private List<Caller<T>> dependants;
    private boolean forwardDeps = false;
    
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
    
    public CallerBuilder<T> forwardDependeciesAsArguments(){
        this.forwardDeps = true;
        return this;
    }
    
    public Caller<T> toResultCall(Function<List<T>, T> call) {
        return toCall(args -> Caller.ofResult(call.apply(args)));
    }
    
    public Caller<T> toResultCall(Supplier<T> call) {
        return toCall(args -> Caller.ofResult(call.get()));
    }
    
    public Caller<T> toCall(Function<List<T>, Caller<T>> call) {
        if(forwardDeps){
            Function<List<T>, Caller<T>> call2 = args ->{
                return call.apply(args).withForwardDeps(true);
            };
            return new Caller<>(CallerType.FUNCTION, null, call2, this.dependants).withForwardDeps(forwardDeps);
        }else{
            return new Caller<>(CallerType.FUNCTION, null, call, this.dependants);
        }
        
    }
    
    public Caller<T> toCall(Supplier<Caller<T>> call) {
        return toCall(args -> Caller.ofSupplier(call));
    }
}
