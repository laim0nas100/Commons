package lt.lb.zk.builder;

import java.util.List;
import com.github.laim0nas100.commonslb.containers.collections.Props;

/**
 *
 * @author laim0nas100
 */
public interface CTX {

    public List<Prop> getProperties();

    public Props<String> getData();
}
