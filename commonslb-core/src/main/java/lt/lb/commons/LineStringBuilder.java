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

    @Override
    public LineStringBuilder appendLine(Object... objects) {
        this.append(objects);
        sb.append(lineEnding);
        return this;
    }

    @Override
    public LineStringBuilder append(Object... objects) {
        for (Object ob : objects) {
            sb.append(ob);
        }
        return this;
    }

    @Override
    public LineStringBuilder append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    public LineStringBuilder append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    @Override
    public LineStringBuilder insert(int offset, Object... objects) {
        StringBuilder temp = createBuilderOf(objects);
        sb.insert(offset, temp.toString());
        return this;
    }

    @Override
    public LineStringBuilder insertLine(int offset, Object... objects) {
        StringBuilder temp = createBuilderOf(objects).append(lineEnding);
        sb.insert(offset, temp.toString());
        return this;
    }

    @Override
    public LineStringBuilder prepend(Object... objects) {
        StringBuilder temp = createBuilderOf(objects);
        sb.insert(0, temp.toString());
        return this;
    }

    @Override
    public LineStringBuilder prependLine(Object... objects) {
        StringBuilder temp = createBuilderOf(objects).append(lineEnding);
        sb.insert(0, temp.toString());
        return this;
    }

    public LineStringBuilder delete(int from, int to) {
        sb.delete(from, to);
        return this;
    }

    public LineStringBuilder deleteCharAt(int at) {
        sb.deleteCharAt(at);
        return this;
    }

    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    public LineStringBuilder reverse() {
        sb.reverse();
        return this;
    }

    public LineStringBuilder replace(int from, int to, String str) {
        sb.replace(from, to, str);
        return this;
    }

    public LineStringBuilder replace(String str) {
        return this.replace(0, length(), str);
    }

    public LineStringBuilder setCharAt(int index, Character... chars) {
        for (int i = index; i < chars.length; i++) {
            sb.setCharAt(i, chars[i]);
        }
        return this;
    }

    @Override
    public int length() {
        return sb.length();
    }

    public LineStringBuilder appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    public int codePointCount(int begin, int end) {
        return sb.codePointCount(begin, end);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] chars, int destBegin) {
        sb.getChars(srcBegin, srcEnd, chars, destBegin);
    }

    public String clear() {
        String value = toString();
        delete(0, length());
        return value;
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

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

    @Override
    public String toString() {
        return sb.toString();
    }

    public LineStringBuilder removeFromStart(int length) {
        return delete(0, length);
    }

    public LineStringBuilder removeFromEnd(int length) {
        int lastChar = this.length();
        int firstChar = lastChar - length;
        return delete(firstChar, lastChar);
    }

    @Override
    public Appendable append(CharSequence arg0) {
        sb.append(arg0);
        return this;
    }

    @Override
    public LineStringBuilder append(char arg0) {
        sb.append(arg0);
        return this;
    }

}
