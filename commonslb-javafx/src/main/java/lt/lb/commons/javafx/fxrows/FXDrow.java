package lt.lb.commons.javafx.fxrows;

import java.util.function.Supplier;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lt.lb.commons.iteration.For;
import lt.lb.commons.rows.SyncDrow;

/**
 * @author laim0nas100
 */
public class FXDrow extends SyncDrow<FXCell, Node, FXLine, FXUpdates, FXDrowConf, FXDrow> {

    public FXDrow(FXLine line, FXDrowConf config, String key) {
        super(line, config, key);
    }

    public FXDrow addButton(String str, EventHandler<ActionEvent> eh) {
        Button b = new Button(str);
        b.setOnAction(eh);
        return add(b);
    }

    public FXDrow addLabel(String str) {
        return add(new Label(str));
    }
    
    public FXDrow addLabelUpdate(Supplier<String> str) {
        Label label = new Label();
        this.withUpdateRefresh(row ->{
           label.setText(str.get());
        });
        return add(label);
    }

    public FXDrow addFxSync(FXSync sync) {
        addSync(sync);
        addSyncValidation(sync);
        return add(sync.getNode());
    }

    @Override
    public FXDrow display() {
        return display(false);
    }

    public FXDrow withPreferedAlign(HPos... pos) {
        addOnDisplayAndRunIfDone(() -> {
            For.elements().iterate(pos, (i, spa) -> {
                FXCell cell = this.getCell(i);
                cell.setAlign(spa);

            });
            update();
        });

        return this;
    }

    @Override
    public FXDrow me() {
        return this;
    }

}
