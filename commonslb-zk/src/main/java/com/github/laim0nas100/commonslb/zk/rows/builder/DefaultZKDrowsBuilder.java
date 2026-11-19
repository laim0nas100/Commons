package com.github.laim0nas100.commonslb.zk.rows.builder;

import java.util.HashMap;
import java.util.Map;
import com.github.laim0nas100.commonslb.zk.builder.CTX;
import com.github.laim0nas100.commonslb.zk.rows.ZKBaseDrow;
import com.github.laim0nas100.commonslb.zk.rows.ZKBaseDrowConf;
import com.github.laim0nas100.commonslb.zk.rows.ZKBaseDrows;
import com.github.laim0nas100.commonslb.zk.rows.ZKBaseDrowsConfig;
import org.zkoss.zk.ui.Component;

/**
 *
 * @author laim0nas100
 */
public abstract class DefaultZKDrowsBuilder<R extends ZKBaseDrow<R, RR>, RR extends ZKBaseDrows<R, RR>, C extends CTX> implements ZKRowsBuilder<C, RR> {

    public static class RowsInfo<C extends CTX, R extends ZKBaseDrow<R, RR>, RR extends ZKBaseDrows<R, RR>> {

        public RowsInfo(C context, RR rows, ZKBaseDrowConf<R, RR> rowConf, ZKBaseDrowsConfig<R, RR> rowsConf) {
            this.context = context;
            this.rows = rows;
            this.rowConf = rowConf;
            this.rowsConf = rowsConf;
        }

        public final C context;
        public final RR rows;
        public final ZKBaseDrowConf<R, RR> rowConf;
        public final ZKBaseDrowsConfig<R, RR> rowsConf;

    }

    protected Map<C, RowsInfo<C, R, RR>> contextualized = new HashMap<>();

    public RowsInfo<C, R, RR> getInfo(C context) {
        return contextualized.computeIfAbsent(context, k -> getNewInfo(k));
    }

    public RowsInfo<C, R, RR> replaceInfo(C context) {
        RowsInfo<C, R, RR> info = getNewInfo(context);
        contextualized.put(info.context, info);
        return info;
    }

    public abstract void constructLogic(Component root, RR rows, C context) throws Exception;

    @Override
    public RR build(Component root, C context) throws Exception {
        RR rows = getRows(context);
        rows.grid.setParent(root);
        constructLogic(root, rows, context);
        rows.syncManagedFromPersist();
        rows.syncDisplay();

        return rows;
    }

    public RR getRows(C context) {
        return getInfo(context).rows;
    }

    @Override
    public abstract C getNewContext();

    public abstract RowsInfo<C, R, RR> getNewInfo(C context);

}
