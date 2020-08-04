package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public final class SetOnce<T> implements ValueProxy<T> {

    protected T val;
    protected boolean set = false;
    
    @Override
    public T get() {
        if(!isSet()){
            throw new IllegalStateException("Value was not set");
        }
        return val;
    }

    @Override
    public void set(T v) {
        if(set){
            throw new IllegalStateException("Value was allready set");
        }
        set = true;
        this.val = v;
    }
    
    public boolean isSet(){
        return set;
    }
    
}
