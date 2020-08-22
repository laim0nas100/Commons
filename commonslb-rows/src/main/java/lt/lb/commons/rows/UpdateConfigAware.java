package lt.lb.commons.rows;

import java.util.Map;

/**
 *
 * @author laim0nas100
 * @param <U> Updates
 * @param <T> Object that is UpdateAware
 */
public interface UpdateConfigAware<U extends Updates, T extends UpdateAware> {

    /**
     * Updates factory method.
     *
     * @param type
     * @param object
     * @return
     */
    public U createUpdates(String type, T object);

    /**
     * Updates execution strategy.
     *
     * @param updates
     * @param object
     */
    public void doUpdates(U updates, T object);

    /**
     * Updates configuration strategy. Usually in initialization.
     *
     * @param updates
     * @param object
     */
    public void configureUpdates(Map<String, U> updates, T object);

}
