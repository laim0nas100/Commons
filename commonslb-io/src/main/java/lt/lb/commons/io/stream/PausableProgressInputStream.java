package lt.lb.commons.io.stream;

import java.io.IOException;
import lt.lb.commons.threads.sync.ConditionalWait;

/**
 *
 * @author laim0nas100
 */
public abstract class PausableProgressInputStream extends ForwardingInputStream {

    protected final ConditionalWait wait = new ConditionalWait();
    protected long bytesRead = 0L;

    protected boolean closed = false;
    protected boolean inRead = false;

    public boolean isPaused() {
        return wait.isInWait();
    }

    public void pause() {
        wait.requestWait();
    }

    public void unpause() {
        wait.wakeUp();
    }

    protected abstract void updateProgress(long bytesRead);

    @Override
    public int read() throws IOException {
        if (inRead) {
            return super.read();
        }
        if (closed) {
            return -1;
        }
        inRead = true;
        int read = -1;
        try {
            wait.conditionalWait();
            read = super.read();
            if (read == -1) {
                return read;
            }
            bytesRead += 1;
            updateProgress(bytesRead);
        } finally {
            inRead = false;
        }

        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (inRead) {
            return super.read(b);
        }
        if (closed) {
            return -1;
        }
        inRead = true;
        int read = -1;
        try {
            wait.conditionalWait();
            read = super.read(b);
            if (read == -1) {
                return read;
            }

            bytesRead += read;
            updateProgress(bytesRead);
        } finally {
            inRead = false;
        }

        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (inRead) {
            return super.read(b, off, len);
        }
        if (closed) {
            return -1;
        }
        inRead = true;
        int read = -1;
        try {
            wait.conditionalWait();
            read = super.read(b, off, len);
            if (read == -1) {
                return read;
            }
            bytesRead += read;
            updateProgress(bytesRead);
        } finally {
            inRead = false;
        }

        return read;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }

}
