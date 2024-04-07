package lt.lb.commons;

import lt.lb.commons.interfaces.StringBuilderActions.ILineStringBuilder;

/**
 *
 * String builder that supports adding lines. Default line ending is \n
 *
 * @author laim0nas100
 */
public class LineStringBuilder implements java.io.Serializable, CharSequence, ILineStringBuilder, Appendable {

    private final StringBuilder sb;
    public final String lineEnding;
    public static final String UNIX_LINE_END = "\n";
    public static final String DOS_LINE_END = "\r\n";

    public LineStringBuilder(String lineEnding) {
        this.lineEnding = lineEnding;
        this.sb = new StringBuilder();
    }

    public LineStringBuilder() {
        this(UNIX_LINE_END);
    }

    /**
     * Appends objects and then adds a line ending
     *
     * @param objects
     * @return this
     */
    @Override
    public LineStringBuilder appendLine(Object... objects) {
        this.append(objects);
        sb.append(lineEnding);
        return this;
    }

    /**
     * Appends each object followed by a line ending
     *
     * @param objects
     * @return
     */
    public LineStringBuilder appendAsLines(Object... objects) {
        for (Object ob : objects) {
            sb.append(ob).append(lineEnding);
        }
        return this;
    }

    /**
     * Appends objects
     *
     * @param objects
     * @return this
     */
    @Override
    public LineStringBuilder append(Object... objects) {
        for (Object ob : objects) {
            sb.append(ob);
        }
        return this;
    }

    /**
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public LineStringBuilder append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    /**
     *
     * @param str the characters to be appended.
     * @param offset the index of the first {@code char} to append.
     * @param len the number of {@code char}s to append.
     * @return a reference to this object.
     * @throws IndexOutOfBoundsException if {@code offset < 0} or
     * {@code len < 0} or {@code offset+len > str.length}
     */
    public LineStringBuilder append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    /**
     * Appends objects at given offset.
     *
     * @param offset the index of the first {@code char} to append.
     * @param objects objects to append
     * @return
     */
    @Override
    public LineStringBuilder insert(int offset, Object... objects) {
        sb.insert(offset, createBuilderOf(objects).toString());
        return this;
    }

    /**
     * Appends objects and a new line at given offset.
     *
     * @param offset the index of the first {@code char} to append.
     * @param objects objects to append
     * @return
     */
    @Override
    public LineStringBuilder insertLine(int offset, Object... objects) {
        sb.insert(offset, createBuilderOf(objects).append(lineEnding).toString());
        return this;
    }

    /**
     * Prepends objects.
     *
     * @param objects objects to prepend
     * @return
     */
    @Override
    public LineStringBuilder prepend(Object... objects) {
        StringBuilder temp = createBuilderOf(objects);
        sb.insert(0, temp.toString());
        return this;
    }

    /**
     * Prepends objects and a new line.
     *
     * @param objects objects to prepend
     * @return
     */
    @Override
    public LineStringBuilder prependLine(Object... objects) {
        StringBuilder temp = createBuilderOf(objects).append(lineEnding);
        sb.insert(0, temp.toString());
        return this;
    }

    /**
     * Delete characters in the interval [from,to)
     *
     * @param from index inclusive
     * @param to index exclusive
     * @return
     */
    public LineStringBuilder delete(int from, int to) {
        sb.delete(from, to);
        return this;
    }

    /**
     * Delete characters in the interval [from,to) if possible
     *
     * @param from index inclusive
     * @param to index exclusive
     * @return
     */
    public LineStringBuilder deleteIfPossible(int from, int to) {
        int len = this.length();
        if (from <= len && to <= len && from < to && from >= 0) {
            sb.delete(from, to);
        }

        return this;
    }

    /**
     * Deletes a single {@code char} at a given index
     *
     * @param at index to delete char from
     * @return
     */
    public LineStringBuilder deleteCharAt(int at) {
        sb.deleteCharAt(at);
        return this;
    }

    /**
     * Finds first appearance of given substring
     *
     * @param str
     * @return
     */
    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    /**
     * Finds first appearance of given substring with a given starting index
     *
     * @param str
     * @param fromIndex starting index
     * @return
     */
    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    /**
     * Finds last appearance of given substring
     *
     * @param str
     * @return
     */
    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    /**
     * Finds last appearance of given substring with a given starting index
     *
     * @param str
     * @return
     */
    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    /**
     * Reverse the character array
     *
     * @return
     */
    public LineStringBuilder reverse() {
        sb.reverse();
        return this;
    }

    /**
     * Delete characters within specified interval and insert a given string
     *
     * @param from
     * @param to
     * @param str
     * @return
     */
    public LineStringBuilder replace(int from, int to, String str) {
        sb.replace(from, to, str);
        return this;
    }

    /**
     * Replace the whole thing with a given string
     *
     * @param str
     * @return
     */
    public LineStringBuilder replace(String str) {
        return this.replace(0, length(), str);
    }

    /**
     * Replace character at specific index
     *
     * @param index
     * @param c
     * @return
     */
    public LineStringBuilder setCharAt(int index, char c) {
        sb.setCharAt(index, c);
        return this;
    }

    /**
     * {@link StringBuilder#length}
     */
    @Override
    public int length() {
        return sb.length();
    }

    /**
     * {@link StringBuilder#appendCodePoint}
     */
    public LineStringBuilder appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    /**
     * {@link StringBuilder#codePointAt}
     */
    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    /**
     * {@link StringBuilder#codePointBefore}
     */
    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    /**
     * {@link StringBuilder#codePointCount}
     */
    public int codePointCount(int begin, int end) {
        return sb.codePointCount(begin, end);
    }

    /**
     * {@link StringBuilder#offsetByCodePoints}
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * {@link StringBuilder#getChars}
     */
    public void getChars(int srcBegin, int srcEnd, char[] chars, int destBegin) {
        sb.getChars(srcBegin, srcEnd, chars, destBegin);
    }

    /**
     * Deletes everything and returns what was before deletion.
     *
     * @return
     */
    public String clear() {
        String value = toString();
        delete(0, length());
        return value;
    }

    /**
     * {@link StringBuilder#charAt}
     */
    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    /**
     * {@link StringBuilder#subSequence}
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    /**
     * {@link StringBuilder#trimToSize}
     */
    public void trimToSize() {
        sb.trimToSize();
    }

    /**
     *
     * @return backing StringBuilder object
     */
    public StringBuilder getStringBuilder() {
        return sb;
    }

    public static StringBuilder createBuilderOf(Object... objects) {
        StringBuilder temp = new StringBuilder();
        for (Object ob : objects) {
            temp.append(ob);
        }
        return temp;
    }

    /**
     * Creates a {@code String} from collected characters.
     * {@link StringBuilder#toString}
     *
     * @return
     */
    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Delete given amount of characters from the start
     *
     * @param length
     * @return
     */
    public LineStringBuilder removeFromStart(int length) {
        return delete(0, length);
    }

    /**
     * Delete given amount of characters from the start if possible
     *
     * @param length
     * @return
     */
    public LineStringBuilder removeFromStartIfPossible(int length) {
        if (length <= this.length()) {
            return removeFromStart(length);
        }
        return this;
    }

    /**
     * Delete given amount of characters from the end
     *
     * @param length
     * @return
     */
    public LineStringBuilder removeFromEnd(int length) {
        int lastChar = this.length();
        int firstChar = lastChar - length;
        return delete(firstChar, lastChar);
    }

    /**
     * Delete given amount of characters from the end if possible
     *
     * @param length
     * @return
     */
    public LineStringBuilder removeFromEndIfPossible(int length) {
        if (length <= this.length()) {
            return removeFromEnd(length);
        }
        return this;
    }

    /**
     * Appends a character sequence
     *
     * @param arg0
     * @return
     */
    @Override
    public LineStringBuilder append(CharSequence arg0) {
        sb.append(arg0);
        return this;
    }

    /**
     * Appends a character
     *
     * @param arg0
     * @return
     */
    @Override
    public LineStringBuilder append(char arg0) {
        sb.append(arg0);
        return this;
    }

}
