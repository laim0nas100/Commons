package com.github.laim0nas100.commonslb.rows.base;

import com.github.laim0nas100.commonslb.rows.SyncDrow;
import com.github.laim0nas100.commonslb.rows.SyncDrowConf;
import com.github.laim0nas100.commonslb.rows.Updates;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseDrowSyncConf<R extends SyncDrow, C, N, L, U extends Updates, Conf extends BaseDrowSyncConf> extends BaseDrowBindsConf<R, C, N, L, U, Conf> implements SyncDrowConf<R, C, N, L, U> {

}
