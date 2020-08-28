package lt.lb.commons.rows.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lt.lb.commons.rows.CellInf;

/**
 *
 * @author laim0nas100
 * @param <N>
 * @param <Enc>
 */
public class BaseCell<N, Enc extends N> implements CellInf<N> {

    protected boolean visible = true;
    protected boolean disabled = false;
    protected boolean merged = false;
    protected int colspan = 1;
    protected List<N> children = new ArrayList<>();
    protected Optional<Enc> enclosed = Optional.empty();

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isMerged() {
        return merged;
    }

    @Override
    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public Optional<Enc> getEnclosed() {
        return enclosed;
    }

    public void setEnclosed(Enc enclosed) {
        this.enclosed = Optional.ofNullable(enclosed);
    }

    public void setNodes(List<N> list){
        this.children = list;
    }
    
    @Override
    public List<N> getNodes() {
        return children;
    }

    @Override
    public int getColSpan() {
        return colspan;
    }

    @Override
    public void setColSpan(int colspan) {
        this.colspan = colspan;
    }

}
