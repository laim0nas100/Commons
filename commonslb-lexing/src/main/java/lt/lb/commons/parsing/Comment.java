package lt.lb.commons.parsing;

import java.util.Arrays;

/**
 *
 * @author laim0nas100
 */
public class Comment extends Token {

    public final int[] endPos;

    public Comment(String value, int[] pos, int[] endPos) {
        super(value, pos);
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        return "Comment" + super.toString() + Arrays.toString(endPos);
    }
    
    

}
