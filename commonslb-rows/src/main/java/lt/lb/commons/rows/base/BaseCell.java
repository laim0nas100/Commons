package lt.lb.commons.rows.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author laim0nas100
 * @param <N>
 * @param <Enc>
 */
public class BaseCell<N, Enc extends N> {

    protected boolean visible = true;
    protected boolean disabled = false;
    protected boolean merged = false;
    protected int colspan = 1;
    protected List<N> children = new ArrayList<>();
    protected Optional<Enc> enclosed = Optional.empty();

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public List<N> getChildren() {
        return children;
    }

    public void setChildren(List<N> children) {
        this.children = children;
    }

    public Optional<Enc> getEnclosed() {
        return enclosed;
    }

    public void setEnclosed(Enc enclosed) {
        this.enclosed = Optional.ofNullable(enclosed);
    }

}
