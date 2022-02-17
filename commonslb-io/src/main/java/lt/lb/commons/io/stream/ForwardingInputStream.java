package lt.lb.commons.io.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ForwardingInputStream extends InputStream {

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
    public void reset() throws IOException {
        delegate().reset();
    }

    @Override
    public void mark(int readlimit) {
        delegate().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate().markSupported();
    }

}
