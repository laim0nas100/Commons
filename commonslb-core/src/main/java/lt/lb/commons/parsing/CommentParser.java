package lt.lb.commons.parsing;

import java.util.ArrayList;
import java.util.Collection;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.containers.values.StringValue;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class CommentParser {

    private static boolean nullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     *
     * @param lines
     * @param lineComment pass null or empty to disable line comments
     * @param commentStart pass null or empty to disable comments
     * @param commentEnd pass null or empty to disable comments
     * @return ArrayList or parsedLines
     */
    public static ArrayList<String> parseAllComments(Collection<String> lines, String lineComment, String commentStart, String commentEnd) {
        return parseAllComments(ReadOnlyIterator.of(lines), lineComment, commentStart, commentEnd).toArrayList();
    }

    /**
     *
     * @param lines lazily populated lines
     * @param lineComment pass null or empty to disable line comments
     * @param commentStart pass null or empty to disable comments
     * @param commentEnd pass null or empty to disable comments
     * @return ReadOnlyIterator or parsedLines
     */
    public static ReadOnlyIterator<String> parseAllComments(ReadOnlyIterator<String> lines, String lineComment, String commentStart, String commentEnd) {
        final boolean scanLine = !nullOrEmpty(lineComment);
        final boolean scanComment = !(nullOrEmpty(commentStart) || nullOrEmpty(commentEnd));
        final int lenS = scanComment ? commentStart.length() : 0;
        final int lenE = scanComment ? commentEnd.length() : 0;

        BooleanValue inComment = BooleanValue.FALSE();
        ReadOnlyIterator<String> iter = new ReadOnlyIterator<String>() {

            IntegerValue i = new IntegerValue(-1);
            StringValue current = new StringValue();

            @Override
            public boolean hasNext() {
                return lines.hasNext();
            }

            @Override
            public String next() {

                String nextLine = null;
                while (nextLine == null) {
                    boolean useBuilder = false;
                    boolean toAdd = true;
                    StringBuilder sb = new StringBuilder();
                    String str = null;
                    if (lines.hasNext()) {
                        str = lines.next();
                    } else {
                        return null;
                    }
                    while (true) {

                        if (scanComment && inComment.get()) {//look for comment end
                            if (str.contains(commentEnd)) {

                                int indexOf = StringOp.indexOf(str, commentEnd);
                                str = str.substring(indexOf + lenE);

                                //skip all text until that comment end
                                inComment.setFalse();
                                if (str.isEmpty()) {
                                    toAdd = false;
                                    break;
                                }
                            } else { // just ignore the line
                                toAdd = false;
                                break;
                            }

                        } else {

                            if (scanLine && 0 <= StringOp.indexOf(str, lineComment)) {
                                int indexOf = StringOp.indexOf(str, lineComment);
                                str = str.substring(0, indexOf); // save everything before line comment start
                                if (str.isEmpty()) {  // the comment was at the begining. Line cleared.
                                    toAdd = false;
                                    break;
                                }
                            } else {

                                int iStart = StringOp.indexOf(str, commentStart);
                                if (scanComment && iStart >= 0) {
                                    if (str.substring(iStart + lenS).contains(commentEnd)) {// line contains both comment end and start, so we scan through subcomments
                                        StringBuilder builder = new StringBuilder();
                                        useBuilder = true;
                                        while (true) {
                                            if (!str.contains(commentStart)) {
                                                break;
                                            }
                                            int indexOf = StringOp.indexOf(str, commentStart);
                                            String beforeComment = str.substring(0, indexOf);
                                            builder.append(beforeComment);
                                            str = str.substring(indexOf + lenS);
                                            int iEnd = StringOp.indexOf(str, commentEnd);
                                            if (iEnd >= 0) {
                                                str = str.substring(iEnd + lenE);
                                            } else {
                                                break;
                                            }
                                        } // no longer has multi line comments
                                        sb.append(builder.toString());
                                        if (scanLine && str.contains(lineComment)) {
                                            int indexOf = StringOp.indexOf(str, lineComment);
                                            sb.append(str.substring(0, indexOf));
                                            break;
                                        } else {
                                            sb.append(str);
                                            break;
                                            // just regular line after this point
                                        }
                                    } else {
                                        //simple end line with single comment start
                                        int indexOf = StringOp.indexOf(str, commentStart);
                                        str = str.substring(0, indexOf);
                                        inComment.setTrue();
                                        toAdd = !str.isEmpty();
                                        break;
                                    }
                                } else {//regular line. Just add.
                                    break;
                                }
                            }
                        }
                    }
                    if (useBuilder) {
                        nextLine = sb.toString();
                    } else if (toAdd) {
                        nextLine = str;
                    }
                }
                current.set(nextLine);
                i.incrementAndGet();
                return nextLine;

            }

            @Override
            public String getCurrent() {
                return current.get();
            }

            @Override
            public Integer getCurrentIndex() {
                return i.get();
            }

            @Override
            public void close() {
                lines.close();
            }
        };
        return iter;
    }

    /**
     *
     * @param lines
     * @param lineComment symbol to mark comment start in a line
     * @return
     */
    public static ArrayList<String> parseLineComments(Collection<String> lines, String lineComment) {
        return parseAllComments(lines, lineComment, null, null);
    }

}
