package lt.lb.commons.javafx.fxrows;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lt.lb.commons.F;
import lt.lb.commons.datasync.Valid;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.rows.base.BaseDrowSyncConf;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class FXDrowConf extends BaseDrowSyncConf<FXDrow, FXCell, Node, FXLine, FXUpdates, FXDrowConf> {

    public VPos defaultGridVPos = VPos.CENTER;
    public Insets cellInsets = new Insets(5, 10, 5, 10);

    public FXDrowConf() {
        withUpdateDisplay(r -> {
            r.getCells().forEach(cell -> {
                cell.getEnclosed().ifPresent(pane -> {
                    pane.setPadding(cellInsets);
                });
            });
        });
    }

    @Override
    public Node getEnclosingNode(FXDrow r) {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    @Override
    public FXCell createCell(List<Node> nodes, Node enclosingNode, FXDrow r) {
        FXCell cell = new FXCell();
        cell.setNodes(nodes);
        if(enclosingNode instanceof Pane){
            cell.setEnclosed(F.cast(enclosingNode));
        }
        return cell;

    }

    @Override
    public FXUpdates createUpdates(String type, FXDrow r) {
        return new FXUpdates(type);
    }

    @Override
    public void doUpdates(FXUpdates updates, FXDrow drow) {
        updates.commit();
    }

    @Override
    public void renderRow(FXDrow row, boolean dirty) {
        FXLine line = row.getLine();
        FXDrows rows = line.getRows().getLastParentOrMe();
        int rowIndex = rows.getVisibleRowIndex(row.getKey());
        if(!baseDerenderContinue(line, rowIndex, dirty)){
            return;
        }
        
        GridPane grid = rows.grid;
        line.setDerender(() -> {
            for (Node n : line.getRenderedNodes()) {
                grid.getChildren().remove(n);
            }
            line.getCells().clear();
            line.getRenderedNodes().clear();
        });

        if (!row.isActive()) {
            return; // nothing else to do here
        }

        if (rowIndex < 0) {
            throw new IllegalArgumentException(row.getKey() + " was not in " + rows.getComposableKey());
        }

        int colIndex = 0;
        List<FXCell> visibleCells = row.getVisibleCells();
        for (FXCell cell : visibleCells) {
            line.getCells().add(cell);
            Node nodeCell;
            if (cell.getEnclosed().isPresent()) {
                Pane pane = cell.getEnclosed().get();
                pane.getChildren().clear();
                for (Node n : cell.getNodes()) {
                    pane.getChildren().add(n);
                }
                nodeCell = pane;
            } else if (cell.getNodes().size() == 1) {
                nodeCell = cell.getNodes().get(0);
            } else {
                throw new IllegalArgumentException("more than 1 child and no enclosing node, or no children at all");
            }
            line.getRenderedNodes().add(nodeCell);
            ObservableList<Node> children = grid.getChildren();
            children.add(nodeCell);
            GridPane.setColumnIndex(nodeCell, colIndex);

            GridPane.setColumnSpan(nodeCell, cell.getColSpan());
            GridPane.setRowIndex(nodeCell, rowIndex);
            GridPane.setRowSpan(nodeCell, 1);
            GridPane.setValignment(nodeCell, defaultGridVPos);
            GridPane.setHalignment(nodeCell, cell.getAlign());
            colIndex += cell.getColSpan();
        }

        if (line.getRenderedNodes().size() == 1) {
            conditionalAlligment(line.getRenderedNodes().get(0), HPos.CENTER);

        } else if (line.getRenderedNodes().size() == 2) {
            conditionalAlligment(line.getRenderedNodes().get(0), HPos.LEFT);
            conditionalAlligment(line.getRenderedNodes().get(1), HPos.RIGHT);
        } else if (line.getRenderedNodes().size() > 0) {
            final int last = line.getCells().size() - 1;
            For.elements().iterate(line.getRenderedNodes(), (i, n) -> {
                if (i == 0) {
                    conditionalAlligment(n, HPos.LEFT);
                } else if (i == last) {
                    conditionalAlligment(n, HPos.RIGHT);
                } else {
                    conditionalAlligment(n, HPos.CENTER);
                }

            });
        }

    }

    protected void conditionalAlligment(Node n, HPos pos) {
        SafeOpt<Node> of = SafeOpt.of(n);
        HPos halignment = GridPane.getHalignment(n);
        if (halignment == null) {//set default
            of.ifPresent(m -> GridPane.setHalignment(m, pos));
            of.select(VBox.class).ifPresent(m -> m.setAlignment(aligment(pos)));
            of.select(HBox.class).ifPresent(m -> m.setAlignment(aligment(pos)));
        } else { // set given
            of.select(VBox.class).ifPresent(m -> m.setAlignment(aligment(halignment)));
            of.select(HBox.class).ifPresent(m -> m.setAlignment(aligment(halignment)));
        }

    }

    protected Pos aligment(HPos hpos) {
        if (hpos == null) {
            throw new IllegalArgumentException("" + hpos);
        }
        switch (hpos) {
            case CENTER:
                return Pos.CENTER;
            case LEFT:
                return Pos.CENTER_LEFT;
            case RIGHT:
                return Pos.CENTER_RIGHT;
        }
        throw new IllegalArgumentException("" + hpos);
    }

    @Override
    public <M> Valid<M> createValidation(FXDrow row, FXCell cell, Node node, Predicate<M> isValid, Function<? super M, String> error) {
        Objects.requireNonNull(node);
        FXValid<M, Node> valid = new FXValid<>(ReadOnlyIterator.of(node).toArrayList());
        valid.errorSupl = error;
        valid.isValid = isValid;
        return valid;
    }

    @Override
    public <M> Valid<M> createValidation(FXDrow row, Predicate<M> isValid, Function<? super M, String> error) {
        return createValidation(row, null, row.getLine().getRows().grid, isValid, error);

    }

    @Override
    public FXDrowConf me() {
        return this;
    }

}
