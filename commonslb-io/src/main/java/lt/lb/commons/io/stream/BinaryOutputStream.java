package lt.lb.commons.io.stream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <i>Binary output</i>. This class provides methods for converting
 * primtive type variables (<tt>boolean</tt>, <tt>byte</tt>, <tt>char</tt>,
 * <tt>int</tt>, <tt>long</tt>, <tt>float</tt>, and <tt>double</tt>) to
 * sequences of bits and writing them to standard output. Uses big-endian
 * (most-significant byte first).
 * <p>
 * The client must <tt>flush()</tt> the output stream when finished writing
 * bits.
 * <p>
 */
public final class BinaryOutputStream {

    private BufferedOutputStream out;

    private int buffer;     // 8-bit buffer of bits to write out
    private int N;          // number of bits remaining in buffer

    public BinaryOutputStream(String pathToFile) throws IOException {
        out = new BufferedOutputStream(Files.newOutputStream(Paths.get(pathToFile)));
    }

    public BinaryOutputStream(OutputStream stream) throws IOException {
        out = new BufferedOutputStream(stream);
    }

    /**
     * Write the specified bit to standard output.
     */
    private void writeBit(boolean bit) throws IOException {
        // add bit to buffer
        buffer <<= 1;
        if (bit) {
            buffer |= 1;
        }

        // if buffer is full (8 bits), write out as a single byte
        N++;
        if (N == 8) {
            clearBuffer();
        }
    }

    /**
     * Write the 8-bit byte to standard output.
     */
    private void writeByte(int x) throws IOException {
        assert x >= 0 && x < 256;

        // optimized if byte-aligned
        if (N == 0) {
            out.write(x);
            return;
        }

        // otherwise write one bit at a time
        for (int i = 0; i < 8; i++) {
            boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    // write out any remaining bits in buffer to standard output, padding with 0s
    private void clearBuffer() throws IOException {
        if (N == 0) {
            return;
        }
        if (N > 0) {
            buffer <<= (8 - N);
        }
        out.write(buffer);
        N = 0;
        buffer = 0;
    }

    /**
     * Flush standard output, padding 0s if number of bits written so far is not
     * a multiple of 8.
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        clearBuffer();
        out.flush();
    }

    /**
     * Flush and close standard output. Once standard output is closed, you can
     * no longer write bits to it.
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        flush();
        out.close();
    }

    /**
     * Write the specified bit to standard output.
     *
     * @param x the <tt>boolean</tt> to write.
     * @throws java.io.IOException
     */
    public void write(boolean x) throws IOException {
        writeBit(x);
    }

    /**
     * Write the 8-bit byte to standard output.
     *
     * @param x the <tt>byte</tt> to write.
     * @throws java.io.IOException
     */
    public void write(byte x) throws IOException {
        writeByte(x & 0xff);
    }

    /**
     * Write the 32-bit int to standard output.
     *
     * @param x the <tt>int</tt> to write.
     * @throws java.io.IOException
     */
    public void write(int x) throws IOException {
        writeByte((x >>> 24) & 0xff);
        writeByte((x >>> 16) & 0xff);
        writeByte((x >>> 8) & 0xff);
        writeByte((x) & 0xff);
    }

    /**
     * Write the r-bit int to standard output.
     *
     * @param x the <tt>int</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws java.io.IOException
     * @throws RuntimeException if <tt>r</tt> is not between 1 and 32.
     * @throws RuntimeException if <tt>x</tt> is not between 0 and 2<sup>r</sup>
     * - 1.
     */
    public void write(int x, int r) throws IOException {
        if (r == 32) {
            write(x);
        }
        if (r < 1 || r > 32) {
            throw new RuntimeException("Illegal value for r = " + r);
        }
        if (x < 0 || x >= (1 << r)) {
            throw new RuntimeException("Illegal " + r + "-bit char = " + x);
        }
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    /**
     * Write the 64-bit double to standard output.
     *
     * @param x the <tt>double</tt> to write.
     * @throws java.io.IOException
     */
    public void write(double x) throws IOException {
        write(Double.doubleToRawLongBits(x));
    }

    /**
     * Write the 64-bit long to standard output.
     *
     * @param x the <tt>long</tt> to write.
     * @throws java.io.IOException
     */
    public void write(long x) throws IOException {
        writeByte((int) ((x >>> 56) & 0xff));
        writeByte((int) ((x >>> 48) & 0xff));
        writeByte((int) ((x >>> 40) & 0xff));
        writeByte((int) ((x >>> 32) & 0xff));
        writeByte((int) ((x >>> 24) & 0xff));
        writeByte((int) ((x >>> 16) & 0xff));
        writeByte((int) ((x >>> 8) & 0xff));
        writeByte((int) ((x) & 0xff));
    }

    /**
     * Write the 32-bit float to standard output.
     *
     * @param x the <tt>float</tt> to write.
     * @throws java.io.IOException
     */
    public void write(float x) throws IOException {
        write(Float.floatToRawIntBits(x));
    }

    /**
     * Write the 16-bit int to standard output.
     *
     * @param x the <tt>short</tt> to write.
     * @throws java.io.IOException
     */
    public void write(short x) throws IOException {
        writeByte((x >>> 8) & 0xff);
        writeByte((x) & 0xff);
    }

    /**
     * Write the 8-bit char to standard output.
     *
     * @param x the <tt>char</tt> to write.
     * @throws java.io.IOException
     * @throws RuntimeException if <tt>x</tt> is not between 0 and 255.
     */
    public void write(char x) throws IOException {
        if (x < 0 || x >= 256) {
            throw new RuntimeException("Illegal 8-bit char = " + x);
        }
        writeByte(x);
    }

    /**
     * Write the r-bit char to standard output.
     *
     * @param x the <tt>char</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws java.io.IOException
     * @throws RuntimeException if <tt>r</tt> is not between 1 and 16.
     * @throws RuntimeException if <tt>x</tt> is not between 0 and 2<sup>r</sup>
     * - 1.
     */
    public void write(char x, int r) throws IOException {
        if (r == 8) {
            write(x);
        }
        if (r < 1 || r > 16) {
            throw new RuntimeException("Illegal value for r = " + r);
        }
        if (x < 0 || x >= (1 << r)) {
            throw new RuntimeException("Illegal " + r + "-bit char = " + x);
        }
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    /**
     * Write the string of 8-bit characters to standard output.
     *
     * @param s the <tt>String</tt> to write.
     * @throws java.io.IOException
     * @throws RuntimeException if any character in the string is not between 0
     * and 255.
     */
    public void write(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            write(s.charAt(i));
        }
    }

    /**
     * Write the String of r-bit characters to standard output.
     *
     * @param s the <tt>String</tt> to write.
     * @param r the number of relevant bits in each character.
     * @throws java.io.IOException
     * @throws RuntimeException if r is not between 1 and 16.
     * @throws RuntimeException if any character in the string is not between 0
     * and 2<sup>r</sup> - 1.
     */
    public void write(String s, int r) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            write(s.charAt(i), r);
        }
    }

    public void writeStringAsBits(String str) throws IOException {
        for (char c : str.toCharArray()) {
            if (c == '1') {
                write(true);
            } else if (c == '0') {
                write(false);
            }
        }
    }

}
