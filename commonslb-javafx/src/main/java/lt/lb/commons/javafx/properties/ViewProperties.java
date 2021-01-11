package lt.lb.commons.javafx.properties;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableView;
import lt.lb.commons.Ins;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface ViewProperties<T> {

    public static String methodNameArgs(String method, Object... objs) {
        LineStringBuilder sb = new LineStringBuilder(method);
        sb.append("(");
        for (Object o : objs) {
            String val = SafeOpt.ofNullable(o)
                    .map(m -> m.getClass().getSimpleName() + " " + m)
                    .orElse("null");
            sb.append(val).append(", ");
        }
        if (objs.length > 0) {
            sb.removeFromEnd(2);
        }
        return sb.append(")").toString();
    }

    public default <R> R cacheOr(String str, R item) {
        Objects.requireNonNull(item);
        return cacheOrGet(str, () -> item);
    }

    public <R> R cacheOrGet(String str, Supplier<R> supl);

    public ReadOnlyObjectProperty<T> selectedItem();

    public ObservableList<T> selectedItems();

    public ObservableList<T> items();

    public default IntegerBinding selectedSize() {
        return cacheOrGet("selectedSize", () -> Bindings.size(selectedItems()));
    }

    public default IntegerBinding itemsSize() {
        return cacheOrGet("itemsSize", () -> Bindings.size(items()));
    }

    public ReadOnlyIntegerProperty selectedIndex();

    public default BooleanBinding selectedSize(int size) {
        return cacheOrGet(methodNameArgs("selectedSize", size), () -> {
            BooleanBinding equalTo = selectedSize().isEqualTo(size);
            return equalTo;
        });
    }

    public default BooleanBinding itemsSize(int size) {
        return cacheOrGet(methodNameArgs("itemsSize", size), () -> itemsSize().isEqualTo(size));
    }

    public default BooleanBinding selectedItemNull() {
        return cacheOrGet("selectedItemNull", () -> selectedItem().isNull());
    }

    public default BooleanBinding selectedItemNotNull() {
        return cacheOrGet("selectedItemNotNull", () -> selectedItem().isNotNull());
    }

    public default BooleanBinding itemsEmpty() {
        return cacheOrGet("itemsEmpty", () -> itemsSize().isEqualTo(0));
    }

    public default BooleanBinding itemsNotEmpty() {
        return cacheOrGet("itemsNotEmpty", () -> itemsSize().greaterThan(0));
    }

    public static <T> ViewProperties<T> ofListView(ListView<T> view) {
        return of(() -> view.getItems(), () -> view.getSelectionModel());
    }

    public static <T> ViewProperties<T> ofTableView(TableView<T> view) {
        return of(() -> view.getItems(), () -> view.getSelectionModel());
    }

    public static <T> ViewProperties<T> of(Supplier<ObservableList<T>> list, Supplier<MultipleSelectionModel<T>> model) {
        ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
        return new ViewProperties<T>() {
            @Override
            public ReadOnlyObjectProperty<T> selectedItem() {
                return cacheOrGet("selectedItem", () -> model.get().selectedItemProperty());
            }

            @Override
            public ReadOnlyIntegerProperty selectedIndex() {
                return cacheOrGet("selectedIndex", () -> model.get().selectedIndexProperty());
            }

            @Override
            public ObservableList<T> selectedItems() {
                return cacheOrGet("selectedItems", () -> model.get().getSelectedItems());
            }

            @Override
            public ObservableList<T> items() {
                return list.get();
            }

            @Override
            public <R> R cacheOrGet(String str, Supplier<R> supl) {

                if (!cache.containsKey(str)) {
                    R supplied = supl.get();
                    Objects.requireNonNull(supplied);
                    return (R) cache.computeIfAbsent(str, key -> supplied);
                } else {
                    Object get = cache.get(str);
                    R supplied = supl.get();
                    Objects.requireNonNull(supplied);

                    if (!Ins.of(get.getClass()).superClassOf(supplied)) {
                        throw new IllegalArgumentException("Reassigning cache object of different class" + get + " -> " + supplied);
                    } else {
                        cache.put(str, supplied);
                        return supplied;
                    }

                }

            }

        };
    }
}
