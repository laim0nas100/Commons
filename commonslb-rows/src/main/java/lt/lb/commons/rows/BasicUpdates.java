package lt.lb.commons.rows;

/**
 *
 * @author laim0nas100
 */
public abstract class BasicUpdates {

    /**
     * Everything to do during display. Every component should exist as an
     * object, but still not rendered. This is used to give styles, final
     * touches, decoration, etc. Called usually only once, unless the row was
     * cleared. Followed by UPDATES_ON_REFRESH.
     */
    public static final String UPDATES_ON_DISPLAY = "UPDATES_ON_DISPLAY";
    /**
     * Everything to do after anything to do with this row or components
     * changed
     */
    public static final String UPDATES_ON_REFRESH = "UPDATES_ON_REFRESH";

    /**
     * Everything to after update, basically rendering the updated row
     */
    public static final String UPDATES_ON_RENDER = "UPDATES_ON_RENDER";

    /**
     * Everything to do after visible property changed, followed by
     * UPDATES_ON_REFRESH
     */
    public static final String UPDATES_ON_VISIBLE = "UPDATES_ON_VISIBLE";
    /**
     * Everything to do after disabled property changed, followed by
     * UPDATES_ON_REFRESH
     */
    public static final String UPDATES_ON_DISABLE = "UPDATES_ON_DISABLE";
}
