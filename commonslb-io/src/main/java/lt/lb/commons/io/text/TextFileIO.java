package lt.lb.commons.io.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class TextFileIO {

    public static ArrayList<String> readFromFile(String URL) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return readFrom(new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8")));
    }

    public static ArrayList<String> readFrom(BufferedReader in) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        in.lines().forEachOrdered(list::add);
        in.close();
        return list;
    }

    public static ArrayList<String> readFrom(InputStream in) throws IOException {
        return readFrom(new BufferedReader(new InputStreamReader(in, "UTF-8")));
    }

    public static ArrayList<String> readFrom(URL in) throws IOException {
        return readFrom(in.openStream());
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment, String commentStart, String commentEnd) throws FileNotFoundException, IOException {
        return CommentParser.parseAllComments(readFromFile(URL), lineComment, commentStart, commentEnd);
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment) throws FileNotFoundException, IOException {
        return CommentParser.parseLineComments(readFromFile(URL), lineComment);
    }

    public static void writeToFile(String URL, Collection<String> list) throws FileNotFoundException, UnsupportedEncodingException {
        writeToFile(URL, ReadOnlyIterator.of(list));
    }

    public static void writeToFile(String URL, ReadOnlyIterator<String> lines) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(URL, "UTF-8");
                ReadOnlyIterator<String> ln = lines) {
            writeTo(out, ln);
        }
    }

    public static void writeTo(PrintWriter writer, Collection<String> lines) {
        for (String line : lines) {
            writer.println(line);
        }
    }

    public static void writeTo(PrintWriter writer, ReadOnlyIterator<String> lines) {
        for (String line : lines) {
            writer.println(line);
        }
    }

}
