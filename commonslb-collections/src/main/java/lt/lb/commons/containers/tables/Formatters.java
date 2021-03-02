package lt.lb.commons.containers.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lt.lb.commons.containers.ForwardingMap;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 */
public interface Formatters<Format> extends Map<FastID, List<Consumer<Format>>> {

    public static interface ForwardingFormatters<Format> extends Formatters<Format>, ForwardingMap<FastID, List<Consumer<Format>>> {
        
    }

    public static <FORM> Formatters<FORM> ofMap(Map<FastID, List<Consumer<FORM>>> map) {
        Objects.requireNonNull(map);
        return (ForwardingFormatters<FORM>) () -> map;
    }
    
    public static <FORM> Formatters<FORM> getDefault() {
        return ofMap(new HashMap<>());
    }
    
}
