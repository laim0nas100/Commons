package lt.lb.commons.io.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ForwardingOutputStream extends OutputStream {

    public abstract OutputStream delegate();

    @Override
    public void write(int b) throws IOException {
        delegate().write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

}
