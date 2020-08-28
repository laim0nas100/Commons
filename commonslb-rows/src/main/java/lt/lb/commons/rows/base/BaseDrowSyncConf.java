package lt.lb.commons.rows.base;

import lt.lb.commons.rows.SyncDrow;
import lt.lb.commons.rows.SyncDrowConf;
import lt.lb.commons.rows.Updates;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseDrowSyncConf<R extends SyncDrow, C, N, L, U extends Updates, Conf extends BaseDrowSyncConf> extends BaseDrowBindsConf<R, C, N, L, U, Conf> implements SyncDrowConf<R, C, N, L, U> {

}
