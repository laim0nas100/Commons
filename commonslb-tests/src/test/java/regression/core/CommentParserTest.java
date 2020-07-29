package regression.core;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import lt.lb.commons.Log;
import lt.lb.commons.io.TextFileIO;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.parsing.CommentParser;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class CommentParserTest {

    

    public ArrayList<String> make(String... args) {
        ArrayList<String> before = new ArrayList<>(args.length);
        before.addAll(Arrays.asList(args));
        return before;
    }

    @Test
    public void noChange() {
        ArrayList<String> before = make("hello there", "i am a clean text");

        ArrayList<String> after = CommentParser.parseAllComments(before, "//", "/*", "*/");

        assertThat(after).containsExactlyElementsOf(before);
    }

    @Test
    public void lineComments() {
        ArrayList<String> before = make("hello there", "i am a clean text", "//this is a comment", "this is line after comment");

        ArrayList<String> after = CommentParser.parseAllComments(before, "//", "/*", "*/");

        assertThat(after).containsExactly("hello there", "i am a clean text", "this is line after comment");
    }

    @Test
    public void multilineComments() {
        ArrayList<String> before = make("hello there", "i am a clean text", "/*this is a comment", "this is line after comment", "this is comment end */more text", "final line");

        ArrayList<String> after = CommentParser.parseAllComments(before, "//", "/*", "*/");

        assertThat(after).containsExactly("hello there", "i am a clean text", "more text", "final line");
    }

    @Test
    public void multilineLineComments() {
        ArrayList<String> before = make("hello there","", "i am a clean text", "/*this is a comment", "this is line after comment", "//this is a comment/*", "this is comment end */more text", "final line");

        ArrayList<String> after = CommentParser.parseAllComments(before, "//", "/*", "*/");

        assertThat(after).containsExactly("hello there","", "i am a clean text", "more text", "final line");
    }
}
