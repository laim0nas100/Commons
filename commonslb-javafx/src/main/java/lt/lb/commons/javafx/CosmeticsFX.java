package lt.lb.commons.javafx;

import java.util.*;
import javafx.beans.binding.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.*;
import lt.lb.commons.threads.sync.UninterruptibleReadWriteLock;

/**
 *
 * @author laim0nas100
 */
public class CosmeticsFX {

    public static class ExtTableView {

        private class TableCol {

            SortType type;
            TableColumn col;

        }

        public final long resizeTimeout = 500;
        public SimpleBooleanProperty recentlyResized;
        public TimeoutTask resizeTask = new TimeoutTask(resizeTimeout, 10, () -> {
                                                    recentlyResized.set(false);
                                                });
        public ArrayList<TableCol> cols;
        public int sortByColumn;
        public boolean sortable = true;
        public TableView table;
        private UninterruptibleReadWriteLock updateLock = new UninterruptibleReadWriteLock();

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

        public void saveSortPrefereces() {
            if (!this.sortable) {
                return;
            }
            this.updateLock.lockWrite();
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
            this.updateLock.unlockWrite();
        }

        public void setSortPreferences() {
            this.updateLock.lockWrite();
            table.getSortOrder().clear();
            ObservableList sortOrder = table.getSortOrder();
            for (TableCol col : cols) {
                sortOrder.add(col.col);
                col.col.setSortType(col.type);
                col.col.setSortable(true);
            }
            this.updateLock.unlockWrite();
        }

        public void updateContents(ObservableList collection) {
//            this.updateLock.lockWrite();
            table.setItems(collection);
            //Work-around to update table
            TableColumn get = (TableColumn) table.getColumns().get(0);
            get.setVisible(false);
            get.setVisible(true);
//            this.updateLock.unlockWrite();

        }

        public void updateContentsAndSort(Collection collection) {

            saveSortPrefereces();
            updateContents(FXCollections.observableArrayList(collection));
            setSortPreferences();

        }

        public void selectInverted() {
            CosmeticsFX.selectInverted(table.getSelectionModel());
        }

        public ExtTask asynchronousSortTask(ObservableList backingList) {
            Runnable run = () -> {
                updateContentsAndSort(backingList);
            };
            ExtTask task = new ExtTask() {
                @Override
                protected Object call() throws Exception {
                    try {
                        do {
                            FX.submit(run);
                            Thread.sleep(500);
                        } while (!this.canceled.get());
                    } catch (InterruptedException e) {
                    }

                    return 0;
                }
            };
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
}
