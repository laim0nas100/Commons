package lt.lb.commons.containers;

/**
 *
 * @author laim0nas100
 */
public class BooleanValue extends Value<Boolean> {
    
    public static BooleanValue TRUE(){
        return new BooleanValue(Boolean.TRUE);
    }
    
    public static BooleanValue FALSE(){
        return new BooleanValue(Boolean.FALSE);
    }

    public BooleanValue() {
        this(false);
    }

    public BooleanValue(Boolean bool) {
        if (bool == null) {
            this.value = Boolean.FALSE;
        } else {
            this.value = bool;
        }
    }

    public Boolean not() {
        return !this.get();
    }

    public void setFalse() {
        this.set(Boolean.FALSE);
    }

    public void setTrue() {
        this.set(Boolean.TRUE);
    }

    public Boolean negateAndGet() {
        return this.setAndGet(() -> this.not());
    }

    public Boolean getAndNegate() {
        return this.getAndSet(() -> this.not());
    }

}
