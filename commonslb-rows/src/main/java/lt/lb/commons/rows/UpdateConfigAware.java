package lt.lb.commons.rows;

import java.util.Map;

/**
 *
 * @author laim0nas100
 */
public interface UpdateConfigAware<U extends Updates,T> {
    
    public U createUpdates(String type, T object);

    public void doUpdates(U updates, T object);

    public void configureUpdates(Map<String, U> updates, T object);
    
    
}
