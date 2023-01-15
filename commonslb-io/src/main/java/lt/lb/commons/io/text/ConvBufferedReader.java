package lt.lb.commons.io.text;

import java.io.BufferedReader;
import java.io.Reader;

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
}
