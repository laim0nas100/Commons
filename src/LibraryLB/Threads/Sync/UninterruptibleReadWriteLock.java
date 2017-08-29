package LibraryLB.Threads.Sync;

/**
 *
 * @author Laimonas Beniušis
 * Semaphore
 * 
 */
public class UninterruptibleReadWriteLock {
    private int readers = 0;
    private int writers = 0;
    private int writeReq = 0;
    
    public synchronized void lockRead(){
        while(writers + writeReq > 0){
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        readers++;
    }
    
    public synchronized void unlockRead(){
        readers--;
        notifyAll();
    }
    
    public synchronized void lockWrite(){
        writeReq++;
        while(readers + writers > 0){
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        writeReq--;
        writers++;
    }
    public synchronized void unlockWrite(){
        writers--;
        notifyAll();
    }
    
    public boolean isBeingWritten(){
        return writers > 0;
    }
    public boolean isBeingRead(){
        return readers > 0;
    }
}
