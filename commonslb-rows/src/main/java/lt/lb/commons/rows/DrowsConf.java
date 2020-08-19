package lt.lb.commons.rows;


/**
 *
 * @author laim0nas100
 */
public interface DrowsConf<DR extends Drows, R extends Drow, U extends Updates> extends UpdateConfigAware<U, DR> {

    /**
     * Row factory method with supplied key.
     * @param rows
     * @param key
     * @return 
     */
    public R newRow(DR rows, String key);

    /**
     * Row factory method with default key generation.
     * @param rows
     * @return 
     */
    public R newRow(DR rows);
    
    /**
     * Row key factory.
     * @return 
     */
    public String getNextRowID();

    /**
     * Manage bindings on compose event.
     *
     * @param parentRows
     * @param childRows
     */
    public void composeDecorate(DR parentRows, DR childRows);

    /**
     * Manage bindings on decompose event.
     *
     * @param parentRows
     * @param childRows
     */
    public void uncomposeDecorate(DR parentRows, DR childRows);

    /**
     * Manage bindings on row add event.
     *
     * @param parentRows
     * @param childRows
     */
    public void addRowDecorate(DR parentRows, R childRow);

    /**
     * Manage bindings on row remove event.
     *
     * @param parentRows
     * @param childRows
     */
    public void removeRowDecorate(DR parentRows, R childRow);

}
