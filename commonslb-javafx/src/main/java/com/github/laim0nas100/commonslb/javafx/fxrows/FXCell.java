package com.github.laim0nas100.commonslb.javafx.fxrows;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import com.github.laim0nas100.commonslb.rows.base.BaseCell;

/**
 *
 * @author laim0nas100
 */
public class FXCell extends BaseCell<Node, Pane> {

    protected HPos align;

    public HPos getAlign() {
        return align;
    }

    public void setAlign(HPos align) {
        this.align = align;
    }

}
