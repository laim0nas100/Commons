package lt.lb.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ForwardingExtInputStream extends ExtInputStream {

    public abstract InputStream delegate();

    @Override
    public int available() throws IOException {
        return delegate().available();
    }

    @Override
    public int read() throws IOException {
        return delegate().read();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public synchronized void reset() throws IOException {
        delegate().reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        delegate().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate().markSupported();
    }

}
