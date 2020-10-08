package lt.lb.commons.javafx.fxrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import lt.lb.commons.F;
import lt.lb.commons.containers.caching.LazyValue;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.base.NodeSync;
import lt.lb.commons.datasync.extractors.Extractors;
import lt.lb.commons.func.BiConverter;
import lt.lb.commons.javafx.FXDefs;
import lt.lb.commons.misc.ComparatorBuilder;
import lt.lb.commons.parsing.StringOp;
import org.apache.commons.text.similarity.FuzzyScore;

/**
 *
 * @author laim0nas100
 */
public class FXSync<P, D, N extends Node> extends NodeSync<P, D, N, FXValid<P, N>> {

    public FXSync(N node) {
        super(node);
    }

    public N getNode() {
        return nodes.get(0);
    }

    @Override
    public void syncManagedFromDisplay() {
        this.clearInvalidationPersist(getManaged());
        super.syncManagedFromDisplay();
    }

    @Override
    protected FXValid<P, N> createValidation() {
        return new FXValid<>(this.nodes);
    }

    public static class TextFieldSync<T> extends FXSync<T, String, TextField> {

        public TextFieldSync(TextField node) {
            super(node);
        }

    }

    public static <T> TextFieldSync<T> ofTextFieldFormatted(ValueProxy<T> persistProxy, TextField tf, BiConverter<String, T> conv) {
        TextFieldSync<T> fxSync = new TextFieldSync<>(tf);

        fxSync.withIdentityPersist();
        fxSync.withDisplayGet(conv::getFrom);
        fxSync.withDisplaySet(conv::getBackFrom);
        fxSync.withDisplayProxy(Extractors.quickProxy(tf::getText, tf::setText));
        fxSync.withPersistProxy(persistProxy);

        FXDefs.applyOnFocusChange(tf, field -> {
            fxSync.syncManagedFromDisplay();
        });
        return fxSync;
    }

    public static <T> TextFieldSync<T> ofTextFieldFormattedEnforced(ValueProxy<T> persistProxy, TextField tf, TextFormatter<T> formatter) {
        TextFieldSync<T> fxSync = new TextFieldSync<>(tf);

        tf.setTextFormatter(formatter);
        fxSync.withIdentityPersist();
        StringConverter<T> conv = formatter.getValueConverter();
        fxSync.withDisplayGet(conv::fromString);
        fxSync.withDisplaySet(conv::toString);
        fxSync.withDisplayProxy(Extractors.quickProxy(tf::getText, tf::setText));
        fxSync.withPersistProxy(persistProxy);

        FXDefs.applyOnFocusChange(tf, field -> {
            fxSync.syncManagedFromDisplay();
        });
        return fxSync;
    }

    public static TextFieldSync<String> ofTextField(ValueProxy<String> persistProxy, TextField tf) {
        return ofTextFieldFormattedEnforced(persistProxy, tf, new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER));
    }

    public static TextFieldSync<String> ofTextField(ValueProxy<String> persistProxy) {
        return ofTextField(persistProxy, new TextField());
    }

    public static class TableViewSync<T> extends FXSync<Collection<T>, ObservableList<T>, TableView<T>> {

        public TableViewSync(TableView<T> node) {
            super(node);
        }

    }

    public static <T> TableViewSync<T> ofTableView(TableView<T> view, ValueProxy<Collection<T>> items, List<T> options) {

        TableViewSync<T> sync = new TableViewSync<>(view);
        view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sync.withIdentityPersist();
        sync.withDisplaySup(() -> {
            return view.getSelectionModel().getSelectedItems();
        });
        sync.withDisplaySync(supl -> {
            view.getSelectionModel().clearSelection();
            for (T item : supl) {
                view.getSelectionModel().select(item);
            }
        });
        sync.withPersistProxy(items);
        sync.withDisplayGet(obList -> {
            return new ArrayList<>(obList);
        });
        sync.withDisplaySet(val -> {
            return FXCollections.observableArrayList(val);
        });

        view.setItems(FXCollections.observableArrayList(options));

        view.getSelectionModel().selectedItemProperty().addListener((FXDefs.SimpleChangeListener<T>) item -> {
            sync.syncManagedFromDisplay();
        });
        return sync;
    }

    public static class ListViewSync<T> extends FXSync<Collection<T>, ObservableList<T>, ListView<T>> {

        public ListViewSync(ListView<T> node) {
            super(node);
        }

    }

    public static <T> ListViewSync<T> ofListView(ListView<T> view, ValueProxy<Collection<T>> items, List<T> options, FXDefs.SimpleListViewCallback<T> callback) {

        ListViewSync<T> sync = new ListViewSync<>(view);
        view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sync.withIdentityPersist();

        sync.withPersistProxy(items);

        sync.withDisplaySup(() -> view.getSelectionModel().getSelectedItems());
        sync.withDisplaySync(supl -> {
            view.getSelectionModel().clearSelection();
            for (T item : supl) {
                view.getSelectionModel().select(item);
            }
        });
        sync.withDisplayGet(obList -> {
            return new ArrayList<>(obList);
        });
        sync.withDisplaySet(val -> {
            return FXCollections.observableArrayList(val);
        });

        view.setItems(FXCollections.observableArrayList(options));
        view.setCellFactory(callback);

        view.getSelectionModel().selectedItemProperty().addListener((FXDefs.SimpleChangeListener<T>) item -> {
            sync.syncManagedFromDisplay();
        });
        return sync;
    }

    public static class ComboBoxSync<T> extends FXSync<T, T, ComboBox<T>> {

        public ComboBoxSync(ComboBox<T> node) {
            super(node);
        }

    }

    public static <T extends Enum<T>> ComboBoxSync<T> ofComboBox(ComboBox<T> box, ValueProxy<T> selectedItem, Class<T> cls, Function<T, String> textExtract) {
        List<T> options = new ArrayList<>(EnumSet.allOf(cls));
        return ofComboBox(box, selectedItem, options, textExtract, FXDefs.cellFactoryString(textExtract));
    }

    public static <T> ComboBoxSync<T> ofComboBox(ComboBox<T> box, ValueProxy<T> selectedItem, List<T> options, Function<T, String> textExtract) {
        return ofComboBox(box, selectedItem, options, textExtract, FXDefs.cellFactoryString(textExtract));
    }

    private static class MostMatchedString<T> {

        public final T value;
        public final String asString;
        public final String query;
        public final int fuzzyScore;
        public final int commonPrefixDistance;

        public MostMatchedString(T value, String asString, String query) {
            this(value, asString, query, Locale.getDefault());
        }

        public MostMatchedString(T value, String asString, String query, Locale locale) {
            this.value = value;
            this.asString = asString;
            this.query = query;
            this.commonPrefixDistance = StringOp.getCommonPrefix(asString.toLowerCase(locale), query.toLowerCase(locale)).length();
            this.fuzzyScore = new FuzzyScore(locale).fuzzyScore(asString, query);
        }

        private static final LazyValue<Comparator<MostMatchedString>> lazyComparator = new LazyValue<>(() -> {
            return new ComparatorBuilder<MostMatchedString>()
                    .thenComparingValue(f -> f.commonPrefixDistance) // big is good
                    .thenComparingValue(f -> f.fuzzyScore) // big is good
                    .reverseAll()
                    .build();
        });

        public static Comparator<MostMatchedString> compareMostMatched() {
            return lazyComparator.get();
        }

    }

    public static <T> ComboBoxSync<T> ofComboBox(ComboBox<T> box, ValueProxy<T> selectedItem, List<T> options, Function<T, String> textExtract, FXDefs.SimpleListViewCallback<T> callback) {

        ComboBoxSync<T> sync = new ComboBoxSync<>(box);

        sync.withPersistProxy(selectedItem);
        sync.withIdentityPersist();
        sync.withIdentityDisplay();
        box.setEditable(true);

        Map<String, T> mapped = new LinkedHashMap<>();
        for (T item : options) {
            String key = textExtract.apply(item);
            if (mapped.containsKey(key)) {
                throw new IllegalArgumentException(key + " text extractor produced same key twice");
            }
            mapped.put(textExtract.apply(item), item);
        }
        StringConverter<T> stringConverter = new StringConverter<T>() {
            @Override
            public String toString(T t) {
                if (t == null) {
                    return null;
                }
                return textExtract.apply(t);
            }

            @Override
            public T fromString(String query) {
                if (StringOp.isEmpty(query)) {
                    return null;
                }
                T get = mapped.getOrDefault(query, null);

                if (get == null) { // try to get most matched
                    return mapped.entrySet().stream().map(entry -> {
                        return new MostMatchedString<>(entry.getValue(), entry.getKey(), query);
                    }).min(MostMatchedString.compareMostMatched()).map(m -> m.value).orElse(null);

                }
                return get;
            }
        };
        box.setConverter(stringConverter);
        FXDefs.SimpleListViewCallback<T> factory = FXDefs.cellFactoryString(textExtract);
        sync.withDisplaySup(() -> box.getSelectionModel().getSelectedItem());

        sync.withDisplaySync(supl -> {
            box.getSelectionModel().clearSelection();
            box.getSelectionModel().select(supl);
        });

        box.setItems(FXCollections.observableArrayList(options));
        box.setCellFactory(factory);
        ListCell<T> cell = factory.call(null);
        box.setButtonCell(cell);
        box.setOnAction(eh -> {
            sync.syncManagedFromDisplay();
        });

        return sync;
    }

    public static class CheckBoxSync extends FXSync<Boolean, Boolean, CheckBox> {

        public CheckBoxSync(CheckBox node) {
            super(node);
        }

    }

    public static CheckBoxSync ofCheckBox(CheckBox box, ValueProxy<Boolean> condition) {
        CheckBoxSync sync = new CheckBoxSync(box);

        sync.withPersistProxy(condition);
        sync.withIdentityPersist();
        sync.withIdentityDisplay();

        sync.withDisplaySup(() -> box.isSelected());

        sync.withDisplaySync(supl -> {
            boolean con = F.nullWrap(supl, false);
            box.setSelected(con);
        });

        box.setOnAction(eh -> {
            sync.syncManagedFromDisplay();
        });

        return sync;
    }

    public static class ToggleButtonSync extends FXSync<Boolean, Boolean, ToggleButton> {

        public ToggleButtonSync(ToggleButton node) {
            super(node);
        }

    }

    public static ToggleButtonSync ofToggleButton(ToggleButton box, ValueProxy<Boolean> condition) {
        ToggleButtonSync sync = new ToggleButtonSync(box);

        sync.withPersistProxy(condition);
        sync.withIdentityPersist();
        sync.withIdentityDisplay();

        sync.withDisplaySup(() -> box.isSelected());

        sync.withDisplaySync(supl -> {
            boolean con = F.nullWrap(supl, false);
            box.setSelected(con);
        });

        box.setOnAction(eh -> {
            sync.syncManagedFromDisplay();
        });

        return sync;
    }

}
