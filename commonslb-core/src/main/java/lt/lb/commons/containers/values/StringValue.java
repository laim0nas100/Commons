/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        return this.setAndGet(() -> this.get() + str);
    }
    
    public String getAndAppend(String str){
        return this.getAndSet(() -> this.get() + str);
    }

}
