/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.misc.Range;
import lt.lb.commons.parsing.NumberParsing;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.fxrows.FXDrowsConf;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Lemmin
 */
public abstract class FXDefs {

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

    public static Logger log = LogManager.getLogger(FXDefs.class);

    public static void applyOnFocusChange(TextField tf, Consumer<TextField> consumer) {
        BooleanValue hasChanges = BooleanValue.FALSE();
        tf.textProperty().addListener((FXDefs.SimpleChangeListener<String>) s -> {
            hasChanges.setTrue();
        });
        tf.setOnAction(eh -> {
            hasChanges.setFalse();
            consumer.accept(tf);
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

        Lambda.L3<ListCell<P>, P, Boolean> decorator;

        public SimpleListViewCell(Lambda.L3<ListCell<P>, P, Boolean> decorator) {
            this.decorator = decorator;
        }

        @Override
        protected void updateItem(P item, boolean empty) {
            F.checkedRun(() -> {
                if (decorator == null) {
                    log.error("null decorator");
                }
                super.updateItem(item, empty);
                decorator.apply(this, item, empty);
            }).ifPresent(t -> t.printStackTrace());

        }

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

    public static interface SimpleChangeListener<P> extends ChangeListener<P> {

        @Override
        public default void changed(ObservableValue<? extends P> ov, P t, P t1) {
            onNewValue(t1);
        }

        public void onNewValue(P newValue);

        public static <P> SimpleChangeListener<P> of(Consumer<? super P> val) {
            return (P newValue) -> {
                val.accept(newValue);
            };
        }

    }

}
