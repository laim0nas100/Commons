package lt.lb.commons.io.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class ConvBufferedReader extends BufferedReader implements ConvenientReader {

    public ConvBufferedReader(Reader reader, int size) {
        super(reader, size);
    }

    public ConvBufferedReader(Reader reader) {
        super(reader);
    }

    @Override
    public Reader real() {
        return this;
    }

    public long transferTo(Writer out) throws IOException {
        return ConvenientReader.super.transferTo(out);
    }
}
