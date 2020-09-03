package lt.lb.commons.javafx.fxrows;

import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author laim0nas100
 */
public class FXUpdates extends Updates<FXUpdates>{
    
    public FXUpdates(String type) {
        super(type);
    }

    public FXUpdates(FXUpdates old) {
        super(old);
    }
    

    @Override
    protected FXUpdates me() {
        return this;
    }

    @Override
    public FXUpdates clone() {
        return new FXUpdates(this);
    }

    static AtomicLong atom = new AtomicLong();
    public void commit(){
        if(!this.active){
            return;
        }
            FX.submit(() -> {
                this.triggerUpdate(atom.incrementAndGet());
            });
    }
    
}
