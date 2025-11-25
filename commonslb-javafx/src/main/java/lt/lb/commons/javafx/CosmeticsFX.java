package lt.lb.commons.javafx;

import static java.lang.Thread.sleep;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javafx.beans.binding.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.*;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.sync.Awaiter;

/**
 *
 * @author laim0nas100
 */
public class CosmeticsFX {

    public static class ExtTableView {

        private static class TableCol {

            SortType type;
            TableColumn col;

        }

        private static class ScrollBarState {

            double min;
            double max;
            double value;
            double blockIncrement;
            double unitIncrement;
            double percentage;

            ScrollBarState(ScrollBar bar) {
                this(bar.getMin(), bar.getMax(), bar.getValue(), bar.getBlockIncrement(), bar.getUnitIncrement());
            }

            ScrollBarState(double min, double max, double value, double blockInc, double unitInc) {
                this.min = min;
                this.max = max;
                this.value = value;
                this.blockIncrement = blockInc;
                this.unitIncrement = unitInc;
                percentage = (value - min) / (max - min);
            }

            private void restoreScrollBarPositions(ScrollBar bar) {
                // Set back the position values if they present
                if (bar != null) {
                    bar.setMin(min);
                    bar.setMax(max);
                    bar.setValue(value);
                    bar.setUnitIncrement(unitIncrement);
                    bar.setBlockIncrement(blockIncrement);
                }
            }

            private void restoreRelativeValue(ScrollBar bar) {
                // Set back the position values if they present
                if (bar != null) {
                    double newMax = bar.getMax();
                    double newMin = bar.getMin();

                    double relValue = (newMax - newMin) * percentage;
                    bar.setValue(relValue);
                }
            }
        }

        private static final long resizeTimeout = 500;
        private static final long sortTaskTimeout = 200;
        public SimpleBooleanProperty recentlyResized;
        public TimeoutTask resizeTask = new TimeoutTask(resizeTimeout, 10, () -> {
            recentlyResized.set(false);
        });
        public ArrayList<TableCol> cols;
        public int sortByColumn;
        public boolean sortable = true;
        public TableView table;
        private Optional<ScrollBarState> verticalScroll = Optional.empty();
        private Optional<ScrollBarState> horizontalScroll = Optional.empty();
        private ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();

        public ExtTableView(TableView table) {
            this.table = table;
            defaultValues();
        }

        public ExtTableView() {
            this(null);
        }

        private void defaultValues() {
            cols = new ArrayList<>();
            sortByColumn = 0;
            recentlyResized = new SimpleBooleanProperty();
        }

        public void prepareChangeListeners() {
            table.getColumns().forEach(col -> {
                TableColumn c = (TableColumn) col;
                changeListener(c);
                c.setPrefWidth(90);//optional

            });
        }

        private void changeListener(final TableColumn listerColumn) {
            listerColumn.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                    recentlyResized.set(true);
                    resizeTask.update();
                }
            });
        }

        public void resetScrollData() {
            this.verticalScroll = Optional.empty();
            this.horizontalScroll = Optional.empty();
            getScrollBar(table, Orientation.VERTICAL).ifPresent(m -> m.setValue(0d));
            getScrollBar(table, Orientation.HORIZONTAL).ifPresent(m -> m.setValue(0d));
        }

        public void saveScrollState() {
            this.updateLock.writeLock().lock();
            try {
                this.verticalScroll = getScrollBar(table, Orientation.VERTICAL).map(m -> new ScrollBarState(m));
                this.horizontalScroll = getScrollBar(table, Orientation.HORIZONTAL).map(m -> new ScrollBarState(m));
            } finally {
                this.updateLock.writeLock().unlock();
            }
        }

        public void restoreScrollState() {
            this.updateLock.writeLock().lock();
            try {
                verticalScroll.ifPresent(state -> {
                    getScrollBar(table, Orientation.VERTICAL).ifPresent(scroll -> {
                        state.restoreRelativeValue(scroll);
                    });
                });

                horizontalScroll.ifPresent(state -> {
                    getScrollBar(table, Orientation.HORIZONTAL).ifPresent(scroll -> {
                        state.restoreRelativeValue(scroll);
                    });
                });
            } finally {
                this.updateLock.writeLock().unlock();
            }
        }

        public void saveSortPrefereces() {
            if (!this.sortable) {
                return;
            }
            this.updateLock.writeLock().lock();
            try {
                cols.clear();
                if (!table.getSortOrder().isEmpty()) {
                    Iterator iterator = table.getSortOrder().iterator();
                    while (iterator.hasNext()) {
                        TableColumn col = (TableColumn) iterator.next();
                        TableCol coll = new TableCol();
                        coll.col = col;
                        coll.type = col.getSortType();
                        cols.add(coll);
                    }
                }
            } finally {
                this.updateLock.writeLock().unlock();
            }
        }

        public void setSortPreferences() {
            this.updateLock.writeLock().lock();
            try {
                table.getSortOrder().clear();
                ObservableList sortOrder = table.getSortOrder();
                for (TableCol col : cols) {
                    sortOrder.add(col.col);
                    col.col.setSortType(col.type);
                    col.col.setSortable(true);
                }
            } finally {
                this.updateLock.writeLock().unlock();
            }
        }

        public void updateContents(ObservableList collection, boolean partial) {
            this.updateLock.writeLock().lock();
            try {

                if (!partial) {
                    saveScrollState();
                }
                table.setItems(collection);
                //Work-around to update table
                TableColumn get = (TableColumn) table.getColumns().get(0);
                get.setVisible(false);
                get.setVisible(true);
                if (!partial) {
                    restoreScrollState();
                }

            } finally {
                this.updateLock.writeLock().unlock();
            }

        }

        public void updateContentsAndSort(Collection collection) {
            saveSortPrefereces();
            updateContents(FXCollections.observableArrayList(collection), false);
            setSortPreferences();
        }

        public void updateContentsAndSortPartial(Collection collection) {
            saveSortPrefereces();
            updateContents(FXCollections.observableArrayList(collection), true);
            setSortPreferences();
        }

        public void selectInverted() {
            CosmeticsFX.selectInverted(table.getSelectionModel());
        }

        public ExtTask asynchronousSortTask(List backingList) {
            Runnable run = () -> {
                updateContentsAndSortPartial(backingList);
            };
            final CompletableFuture awaiter = new CompletableFuture<>();
            ExtTask task = new ExtTask() {
                @Override
                protected Object call() throws Exception {
                    try {
                        FX.runAndWait(run);
                        while (!this.canceled.get() && !this.done.get()) {
                            Thread.sleep(sortTaskTimeout);

                            FX.runAndWait(run);
                        }
                    } catch (InterruptedException e) {
                    }

                    return 0;
                }
            };
            task.appendOnDone(h -> {
                awaiter.complete(0);
            });
            return task;
        }
    }

    public static MenuItem wrapSelectContextMenu(MultipleSelectionModel model) {

        BooleanBinding greaterThan1 = Bindings.size(model.getSelectedItems()).greaterThan(0);
        Menu select = new Menu("Select");
        select.visibleProperty().bind(greaterThan1);

        MenuItem selectAll = new MenuItem("All");
        selectAll.setOnAction(eh -> {
            model.selectAll();
        });
        selectAll.visibleProperty().bind(model.selectionModeProperty().isEqualTo(SelectionMode.MULTIPLE).and(greaterThan1));

        MenuItem selectInverted = new MenuItem("Invert selection");
        selectInverted.setOnAction(eh -> {
            selectInverted(model);
        });
        selectInverted.visibleProperty().bind(selectAll.visibleProperty());

        MenuItem selectNone = new MenuItem("None");
        selectNone.setOnAction(eh -> {
            model.clearSelection();
        });
        selectNone.visibleProperty().bind(greaterThan1);
        select.getItems().setAll(selectAll, selectNone, selectInverted);
        return select;

    }

    public static void selectInverted(MultipleSelectionModel sm) {
        ArrayDeque<Integer> array = new ArrayDeque<>(sm.getSelectedIndices());
        sm.selectAll();
        array.forEach(sm::clearSelection);
    }

    public static MenuItem simpleMenuItem(String name, EventHandler onAction, BooleanExpression visibleProperty) {
        MenuItem item = new MenuItem(name);
        item.setOnAction(onAction);
        if (visibleProperty != null) {
            item.visibleProperty().bind(visibleProperty);
        }
        return item;

    }

    public static void simpleMenuBindingWrap(Menu menu) {
        final ArrayDeque<BooleanProperty> list = new ArrayDeque<>();
        menu.getItems().forEach(item -> {
            if (item instanceof Menu) {
                simpleMenuBindingWrap((Menu) item);
            }
            list.add(item.visibleProperty());
        });
        if (list.isEmpty()) {
            return;
        }
        if (list.size() == 1) {
            menu.visibleProperty().bind(list.pollFirst());
        } else if (list.size() > 1) {
            BooleanExpression bind = list.pollFirst();
            for (BooleanExpression b : list) {
                bind = Bindings.or(bind, b);
            }
            menu.visibleProperty().bind(bind);
        }
    }

    public static void simpleMenuBindingWrap(ContextMenu menu) {
        menu.getItems().forEach(item -> {
            if (item instanceof Menu) {
                simpleMenuBindingWrap((Menu) item);
            }
        });
    }

    private static Optional<ScrollBar> getScrollBar(Node parent, Orientation orientation) {
        // Get the ScrollBar with the given Orientation using lookupAll
        for (Node n : parent.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(orientation)) {
                    return Optional.of(bar);
                }
            }
        }
        return Optional.empty();
    }
}
