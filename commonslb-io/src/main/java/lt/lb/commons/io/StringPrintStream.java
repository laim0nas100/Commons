package lt.lb.commons.io;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author laim0nas100
 */
public class StringPrintStream extends PrintStream {

    protected ByteArrayOutputStreamRaw byteArray;

    public StringPrintStream(ByteArrayOutputStreamRaw byteArray) {
        super(byteArray);
        this.byteArray = byteArray;
    }

    public String exportString(Charset charset) {
        return new String(byteArray.rawByteArray(), 0, byteArray.size(), charset);
    }

    public String exportString() {
        return exportString(StandardCharsets.UTF_8);
    }

    public static StringPrintStream getNew() {
        return new StringPrintStream(new ByteArrayOutputStreamRaw());
    }

    public ByteArrayOutputStreamRaw getByteArrayOutputStream() {
        return byteArray;
    }

}
