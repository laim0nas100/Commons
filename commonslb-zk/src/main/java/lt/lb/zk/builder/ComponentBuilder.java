package lt.lb.zk.builder;

import com.github.laim0nas100.uncheckedutils.Checked;
import org.zkoss.zk.ui.Component;

/**
 *
 * @author laim0nas100
 */
public interface ComponentBuilder<C extends CTX, R> {

    public R build(Component root, C context) throws Exception;

    public default R buildSafe(Component root, C context) {
        return Checked.uncheckedCall(() -> build(root, context));
    }

    public C getNewContext();
}
