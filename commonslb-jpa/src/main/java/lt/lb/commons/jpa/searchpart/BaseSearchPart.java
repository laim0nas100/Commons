package lt.lb.commons.jpa.searchpart;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseSearchPart<M extends BaseSearchPart<M>> implements SearchPart<M> {

    protected boolean enabled;
    protected boolean negated;

    public BaseSearchPart() {
    }

    public BaseSearchPart(boolean enabled) {
        this.enabled = enabled;
    }

    protected BaseSearchPart(BaseSearchPart<M> copy) {
        this.enabled = copy.enabled;
        this.negated = copy.negated;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public abstract M clone() throws CloneNotSupportedException;

}
