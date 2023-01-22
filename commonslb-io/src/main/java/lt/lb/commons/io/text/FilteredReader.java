package lt.lb.commons.io.text;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.function.IntPredicate;

/**
 *
 * @author laim0nas100
 */
public class FilteredReader extends ConvBufferedReader {

    protected Reader in;

    protected IntPredicate codepointFilter;

    public FilteredReader(Reader in, IntPredicate codepointFilter) {
        super(in);
        this.in = in;
        this.codepointFilter = Objects.requireNonNull(codepointFilter);
    }

    
    @Override
    public int read(StringBuilder sb, int len) throws IOException {

        int totalRead = 0;
        int remaining = len;
        while (remaining > 0) {
            char[] buffer = new char[remaining];
            int read = in.read(buffer, 0, remaining);

            if (read < 0) {
                break;
            }
            totalRead += read;
            String.valueOf(buffer, 0, read)
                    .codePoints()
                    .filter(codepointFilter)
                    .forEach(sb::appendCodePoint);

            remaining = len - sb.length();
        }

        return totalRead;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        StringBuilder sb = new StringBuilder(len);
        int read = read(sb, len);
        sb.getChars(0, sb.length(), cbuf, off);
        
        return read;
    }

    @Override
    public Reader real() {
        return this;
    }

}
