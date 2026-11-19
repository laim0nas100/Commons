package com.github.laim0nas100.commonslb.zk.dialogchain;

import com.github.laim0nas100.commonslb.zk.DialogDTO;
import java.util.function.Consumer;
import com.github.laim0nas100.commonslb.zk.DWindow;
import org.zkoss.zk.ui.Component;

/**
 *
 * @author laim0nas100
 */
public class ChainDialogPattern<OPT, INFO> {

    public DWindow dialog;
    public DialogDTO<INFO> future = DialogDTO.ofFutureResult();
    public OPT options;
    public Consumer<DialogResult<INFO>> continuation;
    public final Component parentComponent;

    public ChainDialogPattern(Component parentComponent) {
        this.parentComponent = parentComponent;
    }
}
