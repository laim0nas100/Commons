package com.github.laim0nas100.commonslb.rows;

/**
 *
 * @author laim0nas100
 */
public interface RowFactory<R extends Drow, RR extends Drows, Config extends DrowsConf<?, R, ?>> {

    public R createRow(Config config, RR drows, String key);
}
