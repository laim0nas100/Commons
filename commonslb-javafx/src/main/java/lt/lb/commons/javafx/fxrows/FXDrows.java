package lt.lb.commons.javafx.fxrows;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import lt.lb.commons.FastIDGen;
import lt.lb.commons.rows.DrowsConf;
import lt.lb.commons.rows.SyncDrows;

/**
 *
 * @author laim0nas100
 */
public class FXDrows extends SyncDrows<FXDrow, FXLine, FXDrows, FXUpdates> {
    public static final FastIDGen idGen = new FastIDGen();
    public GridPane grid;
    

    public FXDrows(GridPane grid, DrowsConf<FXDrows, FXDrow, FXUpdates> conf, int numCols) {
        this(grid, idGen.getAndIncrement()+"-FXDrows", conf, numCols);
    }

    public FXDrows(GridPane grid, String key, DrowsConf<FXDrows, FXDrow, FXUpdates> conf, int numCols) {
        super(key, conf);
        this.grid = grid;
        grid.getColumnConstraints().clear();
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / numCols);
            grid.getColumnConstraints().add(colConst);
        }
    }
    
    @Override
    public FXDrows me() {
        return this;
    }

}
