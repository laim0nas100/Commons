package lt.lb.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * Universal IO interface for object two-way serialization
 *
 * @author laim0nas100
 */
public interface SerializingStreams<O_IN, O_OUT, IN extends InputStream, OUT extends OutputStream> {

    public default SafeOpt<IN> createInputStream(Path path, OpenOption... options) {
        return SafeOpt.of(path)
                .map(m -> Files.newInputStream(path, options))
                .map(BufferedInputStream::new)
                .map(this::createInputStream);
    }

    public IN createInputStream(InputStream input) throws IOException;

    public O_IN readObjectLogic(IN input) throws Throwable;

    public default SafeOpt<O_IN> streamToObject(IN in) {
        return SafeOpt.of(in).map(input -> {
            try {
                return readObjectLogic(input);
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        });
    }

    public default SafeOpt<O_IN> pathToObject(Path path, OpenOption... options) {
        return createInputStream(path, options).map(this::readObjectLogic);
    }

    public default SafeOpt<OUT> createOutputStream(Path path, OpenOption... options) {
        return SafeOpt.of(path)
                .map(m -> Files.newOutputStream(path, options))
                .map(BufferedOutputStream::new)
                .map(this::createOutputStream);
    }

    public OUT createOutputStream(OutputStream output) throws IOException;

    public void writeObjectLogic(O_OUT object, OUT out) throws Throwable;

    public default SafeOpt<O_OUT> objectToStream(O_OUT object, OUT out) {
        return SafeOpt.of(object).map(o -> {
            try {
                writeObjectLogic(object, out);
                return o;
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        });
    }

    public default SafeOpt<O_OUT> objectToPath(O_OUT object, Path path, OpenOption... options) {
        return createOutputStream(path, options).map(out -> {
            writeObjectLogic(object, out);
            return object;
        });
    }

    public default SafeOpt<O_OUT> objectToPathOverwrite(O_OUT object, Path path) {
        return createOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).map(out -> {
            writeObjectLogic(object, out);
            return object;
        });
    }

    public static interface SerializingObjectStreams<IN, OUT> extends SerializingStreams<IN, OUT, ObjectInputStream, ObjectOutputStream> {

        @Override
        public default ObjectInputStream createInputStream(InputStream input) throws IOException {
            return new ObjectInputStream(input);
        }

        @Override
        public default ObjectOutputStream createOutputStream(OutputStream output) throws IOException {
            return new ObjectOutputStream(output);
        }

    }

    public static interface SerializingBufferedStreams<IN, OUT> extends SerializingStreams<IN, OUT, BufferedInputStream, BufferedOutputStream> {

        @Override
        public default BufferedInputStream createInputStream(InputStream input) throws IOException {
            return new BufferedInputStream(input);
        }

        @Override
        public default BufferedOutputStream createOutputStream(OutputStream output) throws IOException {
            return new BufferedOutputStream(output);
        }
    }

    public static interface SerializingTextStreams extends SerializingBufferedStreams<String, CharSequence> {

        public Charset charset();

        @Override
        public default String readObjectLogic(BufferedInputStream input) throws Throwable {
            StringBuilder sb = new StringBuilder();
            try (InputStreamReader reader = new InputStreamReader(input, charset())) {
                int size = 16;
                int read = 1;
                while (read > 0) {
                    char[] chars = new char[size];
                    read = reader.read(chars);
                    if (read > 0) {
                        sb.append(chars, 0, read);
                        if (read < size) {//buffer end
                            break;
                        } else {
                            size = Math.min(size * 2, 1024);
                        }
                    }

                }
            }
            return sb.toString();
        }

        @Override
        public default void writeObjectLogic(CharSequence string, BufferedOutputStream out) throws Throwable {
            try (OutputStreamWriter writer = new OutputStreamWriter(out, charset())) {
                writer.append(string);
            }
        }

        public static SerializingTextStreams ofCharset(Charset charset) {
            Objects.requireNonNull(charset);
            return () -> charset;
        }

    }

    public static final SerializingTextStreams TEXT = SerializingTextStreams.ofCharset(Charset.defaultCharset());

}
