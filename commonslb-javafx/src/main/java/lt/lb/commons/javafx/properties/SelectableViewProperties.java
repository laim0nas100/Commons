package lt.lb.commons.javafx.properties;

import java.util.HashMap;
import java.util.Map;
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
import lt.lb.commons.MethodCallSignature;

/**
 *
 * @author laim0nas100
 */
public interface SelectableViewProperties<T> extends BindingCache {

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
        return cacheOrGet(signature("selectedSize", size), () -> {
            return selectedSize().isEqualTo(size);
        });
    }

    public default BooleanBinding selectedSizeAtLeast(int size) {
        return cacheOrGet(signature("selectedSizeAtLeast", size), () -> {
            return selectedSize().greaterThanOrEqualTo(size);
        });
    }

    public default BooleanBinding selectedSomething() {
        return selectedSizeAtLeast(1);
    }

    public default BooleanBinding itemsSize(int size) {
        return cacheOrGet(signature("itemsSize", size), () -> itemsSize().isEqualTo(size));
    }

    public default BooleanBinding selectedItemNull() {
        return cacheOrGet("selectedItemNull", () -> selectedItem().isNull());
    }

    public default BooleanBinding selectedItemNotNull() {
        return cacheOrGet("selectedItemNotNull", () -> selectedItem().isNotNull());
    }

    public default BooleanBinding itemsEmpty() {
        return itemsSize(0);
    }

    public default BooleanBinding itemsNotEmpty() {
        return cacheOrGet("itemsNotEmpty", () -> itemsSize().greaterThan(0));
    }

    public static <T> SelectableViewProperties<T> ofListView(ListView<T> view) {
        return of(view::getItems, view::getSelectionModel);
    }

    public static <T> SelectableViewProperties<T> ofTableView(TableView<T> view) {
        return of(view::getItems, view::getSelectionModel);
    }

    public static <T> SelectableViewProperties<T> of(Supplier<ObservableList<T>> list, Supplier<MultipleSelectionModel<T>> model) {
        return new SelectableViewProperties<T>() {
            final HashMap<MethodCallSignature, Object> cache = new HashMap<>();
            @Override
            public ReadOnlyObjectProperty<T> selectedItem() {
                return model.get().selectedItemProperty();
            }

            @Override
            public ReadOnlyIntegerProperty selectedIndex() {
                return model.get().selectedIndexProperty();
            }

            @Override
            public ObservableList<T> selectedItems() {
                return model.get().getSelectedItems();
            }

            @Override
            public ObservableList<T> items() {
                return list.get();
            }

            @Override
            public Map<MethodCallSignature, Object> cache() {
                return cache;
            }
        };
    }
}
