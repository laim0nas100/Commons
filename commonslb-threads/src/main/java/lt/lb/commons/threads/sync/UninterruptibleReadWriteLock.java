package lt.lb.commons.threads.sync;

/**
 *
 * @author laim0nas100 Semaphore
 *
 */
public class UninterruptibleReadWriteLock extends ReadWriteLock {

    @Override
    public synchronized void lockRead() {
        boolean interrupted = false;
        while (writers + writeReq > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        readers++;
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void lockWrite() {
        writeReq++;
        boolean interrupted = false;
        while (readers + writers > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        writeReq--;
        writers++;
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
