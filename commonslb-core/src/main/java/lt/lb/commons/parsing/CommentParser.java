/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.parsing;

import java.util.ArrayList;
import java.util.Collection;
import lt.lb.commons.containers.BooleanValue;
import lt.lb.commons.containers.NumberValue;
import lt.lb.commons.containers.StringValue;
import lt.lb.commons.interfaces.ReadOnlyIterator;

/**
 *
 * @author Lemmin
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
        boolean inComment = false;
        ArrayList<String> finalList = new ArrayList<>();
        boolean scanLine = !nullOrEmpty(lineComment);
        boolean scanComment = !(nullOrEmpty(commentStart) || nullOrEmpty(commentEnd));
        int lenS = scanComment ? commentStart.length() : 0;
        int lenE = scanComment ? commentEnd.length() : 0;

        for (String str : lines) {
            boolean useBuilder = false;
            boolean toAdd = true;
            StringBuilder sb = new StringBuilder();
            while (true) {

                if (scanComment && inComment) {//look for comment end
                    if (str.contains(commentEnd)) {

                        int indexOf = str.indexOf(commentEnd);
                        str = str.substring(indexOf + lenE);

                        //skip all text until that comment end
                        inComment = false;
                        if (str.isEmpty()) {
                            toAdd = false;
                            break;
                        }
                    } else { // just ignore the line
                        toAdd = false;
                        break;
                    }

                } else {

                    if (scanLine && str.contains(lineComment)) {
                        int indexOf = str.indexOf(lineComment);
                        str = str.substring(0, indexOf); // save everything before line comment start
                        if (str.isEmpty()) {  // the comment was at the beggining. Line cleared.
                            toAdd = false;
                            break;
                        }
                    } else {
                        int iStart = str.indexOf(commentStart);
                        if (scanComment && iStart >= 0) {
                            if (str.substring(iStart + lenS).contains(commentEnd)) {// line contains both comment end and start, so we scan through subcomments
                                StringBuilder builder = new StringBuilder();
                                useBuilder = true;
                                while (true) {
                                    if (!str.contains(commentStart)) {
                                        break;
                                    }
                                    int indexOf = str.indexOf(commentStart);
                                    String beforeComment = str.substring(0, indexOf);
                                    builder.append(beforeComment);
                                    str = str.substring(indexOf + lenS);
                                    int iEnd = str.indexOf(commentEnd);
                                    if (iEnd >= 0) {
                                        str = str.substring(iEnd + lenE);
                                    } else {
                                        break;
                                    }
                                } // no longer has multi line comments
                                sb.append(builder.toString());
                                if (scanLine && str.contains(lineComment)) {
                                    int indexOf = str.indexOf(lineComment);
                                    sb.append(str.substring(0, indexOf));
                                    break;
                                } else {
                                    sb.append(str);
                                    break;
                                    // just regular line after this point
                                }
                            } else {
                                //simple end line with single comment start
                                int indexOf = str.indexOf(commentStart);
                                str = str.substring(0, indexOf);
                                inComment = true;
                                break;
                            }
                        } else {//regular line. Just add.
                            break;
                        }
                    }
                }
            }
            if (useBuilder) {
                finalList.add(sb.toString());
            } else if (toAdd) {
                finalList.add(str);
            }

        }

        return finalList;
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

            NumberValue<Integer> i = NumberValue.of(-1);
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

                                int indexOf = str.indexOf(commentEnd);
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

                            if (scanLine && str.contains(lineComment)) {
                                int indexOf = str.indexOf(lineComment);
                                str = str.substring(0, indexOf); // save everything before line comment start
                                if (str.isEmpty()) {  // the comment was at the beggining. Line cleared.
                                    toAdd = false;
                                    break;
                                }
                            } else {
                                int iStart = str.indexOf(commentStart);
                                if (scanComment && iStart >= 0) {
                                    if (str.substring(iStart + lenS).contains(commentEnd)) {// line contains both comment end and start, so we scan through subcomments
                                        StringBuilder builder = new StringBuilder();
                                        useBuilder = true;
                                        while (true) {
                                            if (!str.contains(commentStart)) {
                                                break;
                                            }
                                            int indexOf = str.indexOf(commentStart);
                                            String beforeComment = str.substring(0, indexOf);
                                            builder.append(beforeComment);
                                            str = str.substring(indexOf + lenS);
                                            int iEnd = str.indexOf(commentEnd);
                                            if (iEnd >= 0) {
                                                str = str.substring(iEnd + lenE);
                                            } else {
                                                break;
                                            }
                                        } // no longer has multi line comments
                                        sb.append(builder.toString());
                                        if (scanLine && str.contains(lineComment)) {
                                            int indexOf = str.indexOf(lineComment);
                                            sb.append(str.substring(0, indexOf));
                                            break;
                                        } else {
                                            sb.append(str);
                                            break;
                                            // just regular line after this point
                                        }
                                    } else {
                                        //simple end line with single comment start
                                        int indexOf = str.indexOf(commentStart);
                                        str = str.substring(0, indexOf);
                                        inComment.setTrue();
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
