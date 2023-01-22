package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class StringValue extends Value<String> {

    public StringValue() {
        super();
    }

    public StringValue(String val) {
        super(val);
    }

    public String appendAndGet(String str) {
        return this.setAndGetSupl(() -> this.get() + str);
    }
    
    public String getAndAppend(String str){
        return this.getAndSetSupl(() -> this.get() + str);
    }

}
