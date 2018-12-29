package lt.lb.commons.threads.sync;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author laim0nas100
 */
public class FairThreadBottleneck extends ThreadBottleneck {

    public FairThreadBottleneck(int maxThreads) {
        super(maxThreads);
        ArrayBlockingQueue abq = new ArrayBlockingQueue(maxThreads, true);
        for(int i = 0; i < maxThreads; i++){
            abq.add(dummy);
        }
        q = abq;
    }

    @Override
    protected void reinsert() {
        if(!q.add(dummy)){
            throw new IllegalStateException("Failed to reinsert. Should not happen");
        }
    }

}
