package lt.lb.commons.javafx;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.containers.values.StringValue;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.fxrows.FXDrowsConf;
import lt.lb.commons.misc.Range;
import lt.lb.commons.parsing.NumberParsing;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public abstract class FXDefs {

    public static void closeTab(Tab tab) {
        EventHandler<Event> handler = tab.getOnClosed();
        if (null != handler) {
            handler.handle(null);
        }
        tab.getTabPane().getTabs().remove(tab);
    }

    public static abstract class TextFormatters {

        public static <T> TextFormatter<T> ofSafeConversion(T def, Function<String, SafeOpt<T>> func) {
            StringConverter<T> stringConverter = new StringConverter<T>() {
                @Override
                public String toString(T t) {
                    return String.valueOf(t);
                }

                @Override
                public T fromString(String string) {
                    return func.apply(string).orElse(def);
                }
            };

            UnaryOperator<TextFormatter.Change> filter = change -> {
                SafeOpt<T> apply = func.apply(change.getControlNewText());
                return apply.isPresent() ? change : null;

            };

            return new TextFormatter<>(stringConverter, def, filter);

        }

        public static TextFormatter<Long> wholeNumberFormat(long min, long max) {
            Range<Long> range = Range.of(min, max);
            return ofSafeConversion(0L, t -> {
                return NumberParsing.parseLong(t).filter(val -> range.inRangeInclusive(val));
            });
        }

        public static TextFormatter<Integer> wholeNumberFormat(int min, int max) {
            Range<Integer> range = Range.of(min, max);
            return ofSafeConversion(0, t -> {
                return NumberParsing.parseInt(t).filter(val -> range.inRangeInclusive(val));
            });
        }

        public static TextFormatter<Double> floatNumberFormat(double min, double max) {
            Range<Double> range = Range.of(min, max);
            return ofSafeConversion(0D, t -> {
                return NumberParsing.parseDouble(t).filter(val -> range.inRangeInclusive(val));
            });
        }

        public static TextFormatter<Float> floatNumberFormat(float min, float max) {
            Range<Float> range = Range.of(min, max);
            return ofSafeConversion(0F, t -> {
                return NumberParsing.parseFloat(t).filter(val -> range.inRangeInclusive(val));
            });
        }
    }

    public static void applyOnTextChangeOrEnter(TextInputControl tf, Consumer<TextInputControl> consumer) {
        StringValue value = new StringValue();
        tf.textProperty().addListener((FXDefs.SimpleChangeListener<String>) s -> {
            value.set(tf.getText());
            consumer.accept(tf);
        });

        tf.setOnKeyPressed(kh -> {
            if (kh.getCode() == KeyCode.ENTER) {
                consumer.accept(tf);
            }
        });
    }

    public static <T extends TextInputControl> void applyOnFocusChange(T tf, Consumer<T> consumer) {
        BooleanValue hasChanges = BooleanValue.FALSE();
        tf.textProperty().addListener((FXDefs.SimpleChangeListener<String>) s -> {
            hasChanges.setTrue();
        });

        tf.setOnKeyPressed(kh -> {
            if (kh.getCode() == KeyCode.ENTER) {
                hasChanges.setFalse();
                consumer.accept(tf);
            }
        });
        tf.focusedProperty().addListener((FXDefs.SimpleChangeListener<Boolean>) focus -> {
            if (focus) {
                //started editing do nothing
            } else {
                //exited editing
                if (hasChanges.get()) {
                    hasChanges.setFalse();
                    consumer.accept(tf);
                }
            }
        });
    }

    public static FXDrows fxrows() {
        FXDrowsConf fxDrowsConf = new FXDrowsConf();
        GridPane gridPane = new GridPane();
        FXDrows fxDrows = new FXDrows(gridPane, fxDrowsConf, 100);
        return fxDrows;
    }

    public static <T> void updateContents(TableView<T> table, ObservableList<T> collection) {
//            this.updateLock.lockWrite();
        table.setItems(collection);
        //Work-around to update table
        TableColumn get = (TableColumn) table.getColumns().get(0);
        get.setVisible(false);
        get.setVisible(true);
//            this.updateLock.unlockWrite();

    }

    public static interface SimpleListViewCallback<P> extends Callback<ListView<P>, ListCell<P>> {

    }

    public static class SimpleListViewCell<P> extends ListCell<P> {

        public static Logger logger = LoggerFactory.getLogger(SimpleListViewCell.class);
        Lambda.L3<ListCell<P>, P, Boolean> decorator;

        public SimpleListViewCell(Lambda.L3<ListCell<P>, P, Boolean> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(P item, boolean empty) {
            Checked.checkedRun(() -> {
                if (decorator == null) {
                    return;
                }
                super.updateItem(item, empty);
                decorator.apply(this, item, empty);
            }).ifPresent(t -> logger.error("Error in item update", t));

        }

    }

    public static <N extends Node> void configureDoubleClick(N node, Predicate<N> when, Consumer<N> cons) {
        node.setOnMousePressed((MouseEvent event) -> {
            if (when.test(node) && event.isPrimaryButtonDown()) {
                if (event.getClickCount() > 1) {
                    cons.accept(node);
                    event.consume();
                }
            }
        });
    }

    public static void configureDragNDrop(Node root, Predicate<Dragboard> when, Consumer<Dragboard> consumer) {
        root.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (event.getGestureSource() != root && when.test(dragboard)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        root.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (event.getGestureSource() != root && when.test(dragboard)) {
                consumer.accept(dragboard);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static void configureDragNDropFiles(Node root, Predicate<File> when, Consumer<List<File>> consumer) {
        configureDragNDrop(root, d -> {
            if (!d.hasFiles()) {
                return false;
            }
            List<File> files = d.getFiles();
            for (File f : files) {
                if (when.test(f)) {
                    return true;
                }
            }
            return false;
        }, d -> {
            List<File> files = d.getFiles();
            List<File> collect = files.stream().filter(when).collect(Collectors.toList());
            consumer.accept(collect);
        });
    }

    public static <P> SimpleListViewCallback<P> cellFactory(Lambda.L3<ListCell<P>, P, Boolean> decorator) {
        return (ListView<P> param) -> {
            return new SimpleListViewCell<>(decorator);
        };
    }

    public static <P> SimpleListViewCallback<P> cellFactoryString(String emptyCase, Function<P, String> textExtract) {
        return cellFactory((c, p, empty) -> {
            c.setText(empty || p == null ? emptyCase : textExtract.apply(p));
        });
    }

    public static <P> SimpleListViewCallback<P> cellFactoryString(Function<P, String> textExtract) {
        return cellFactoryString(null, textExtract);
    }

    public static <N extends Number> SimpleChangeListener<N> numberDiffListener(final double diff, Consumer<? super N> consumer) {
        return diffListener((a, b) -> diff < Math.abs(a.doubleValue() - b.doubleValue()), consumer);
    }

    public static <N> SimpleChangeListener<N> diffListener(BiPredicate<N, N> change, Consumer<? super N> consumer) {
        Objects.requireNonNull(change);
        Objects.requireNonNull(consumer);
        return new SimpleChangeListener<N>() {

            N lastAccepted;

            @Override
            public void accept(N newValue) {
                if (lastAccepted == newValue) {
                    return;
                }
                if (Nulls.eitherNull(lastAccepted, newValue) || change.test(lastAccepted, newValue)) {
                    lastAccepted = newValue;
                    consumer.accept(newValue);
                }
            }
        };
    }

    public static interface SimpleChangeListener<P> extends ChangeListener<P>, Consumer<P> {

        @Override
        public default void changed(ObservableValue<? extends P> ov, P t, P t1) {
            accept(t1);
        }

        @Override
        public void accept(P newValue);

        public static <P> SimpleChangeListener<P> of(Consumer<? super P> val) {
            Objects.requireNonNull(val);
            return val::accept;
        }

    }

}
