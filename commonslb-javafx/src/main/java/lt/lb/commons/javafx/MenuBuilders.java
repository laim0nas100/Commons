package lt.lb.commons.javafx;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import lt.lb.commons.MonadicBuilders.StringWithInitialBuilder;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class MenuBuilders {

    public static class MenuBuilder extends BaseMenuItemBuilder<Menu, MenuBuilder> {

        public MenuBuilder() {
            this(Menu::new);
        }

        public MenuBuilder(Supplier<? extends Menu> supl) {
            this.supplier = supl;
        }

        @Override
        protected MenuBuilder copy(Map<String, Function<? super Menu, ? extends Menu>> funcs, int reqSize) {
            return copyLinkedHashMap(() -> new MenuBuilder(this.supplier));
        }

        public MenuBuilder addMenu(Function<MenuBuilder, MenuBuilder> mb) {

            return thenCon(menu -> {
                MenuBuilder newMb = mb.apply(new MenuBuilder(this.supplier));
                menu.getItems().add(newMb.build());
            });
        }

    }

    public static class MenuItemBuilder extends BaseMenuItemBuilder<MenuItem, MenuItemBuilder> {

        public MenuItemBuilder() {
            this(MenuItem::new);
        }

        public MenuItemBuilder(Supplier<? extends MenuItem> supl) {
            this.supplier = supl;
        }

        @Override
        protected MenuItemBuilder copy(Map<String, Function<? super MenuItem, ? extends MenuItem>> funcs, int reqSize) {
            return copyLinkedHashMap(() -> new MenuItemBuilder(this.supplier));
        }

    }

    public static class ContextMenuBuilder extends StringWithInitialBuilder<ContextMenu, ContextMenuBuilder> {

        protected Supplier<? extends MenuItem> itemSupplier;
        protected Supplier<? extends Menu> menuSupplier;

        public ContextMenuBuilder(Supplier<? extends ContextMenu> contextMenuSupplier, Supplier<? extends MenuItem> itemSupplier, Supplier<? extends Menu> menuSupplier) {
            this.itemSupplier = itemSupplier;
            this.menuSupplier = menuSupplier;
            this.supplier = contextMenuSupplier;
            this.functions = new LinkedHashMap<>();
        }

        public ContextMenuBuilder() {
            this(ContextMenu::new, MenuItem::new, Menu::new);
        }

        @Override
        protected ContextMenuBuilder copy(Map<String, Function<? super ContextMenu, ? extends ContextMenu>> funcs, int reqSize) {
            return copyLinkedHashMap(() -> new ContextMenuBuilder(supplier, itemSupplier, menuSupplier));
        }

        public ContextMenuBuilder addItem(MenuItemBuilder builder) {
            return thenCon(c -> c.getItems().add(builder.build()));
        }

        public ContextMenuBuilder addItemMenu(MenuBuilder builder) {
            return thenCon(c -> c.getItems().add(builder.build()));
        }

        public ContextMenuBuilder addNestedVisibilityBind() {
            return thenCon("visibility", c -> {
                simpleMenuBindingWrap(c, m -> m.visibleProperty(), true);
            });
        }

        public ContextMenuBuilder addNestedDisableBind() {
            return thenCon("disabled", c -> {
                simpleMenuBindingWrap(c, m -> m.disableProperty(), false);
            });
        }

    }

    public static class SelectMenuBuilder extends BaseMenuItemBuilder<Menu, SelectMenuBuilder> {

        protected MultipleSelectionModel model;
        protected Supplier<? extends MenuItem> menuItem;

        protected BooleanBinding greaterThan1;
        protected BooleanBinding multiple;

        public SelectMenuBuilder(MultipleSelectionModel model) {
            this(Menu::new, MenuItem::new, model);
        }

        public SelectMenuBuilder(Supplier<? extends Menu> menu, Supplier<? extends MenuItem> menuItem, MultipleSelectionModel model) {
            this.supplier = menu;
            this.menuItem = menuItem;
            this.model = model;
            greaterThan1 = Bindings.size(model.getSelectedItems()).greaterThan(0);
            multiple = Bindings.equal(SelectionMode.MULTIPLE, model.selectionModeProperty());
        }

        @Override
        protected SelectMenuBuilder copy(Map<String, Function<? super Menu, ? extends Menu>> funcs, int reqSize) {
            return copyLinkedHashMap(() -> new SelectMenuBuilder(supplier, menuItem, model));

        }

        public MenuItemBuilder defaultSelectAll() {
            return new MenuItemBuilder(menuItem)
                    .withText("ALL")
                    .withAction(e -> model.selectAll())
                    .visibleWhen(multiple.and(greaterThan1));
        }

        public MenuItemBuilder defaultSelectNone() {
            return new MenuItemBuilder(menuItem)
                    .withText("NONE")
                    .withAction(e -> model.clearSelection())
                    .visibleWhen(greaterThan1);
        }

        public MenuItemBuilder defaultSelectInvert() {
            return new MenuItemBuilder(menuItem)
                    .withText("INVERTED")
                    .withAction(e -> selectInverted(model))
                    .visibleWhen(multiple.and(greaterThan1));
        }

        public SelectMenuBuilder addSelectAll(Function<MenuItemBuilder, MenuItemBuilder> builder) {
            return thenCon("select-all", c -> {
                c.getItems().add(builder.apply(defaultSelectAll()).build());
            });
        }

        public SelectMenuBuilder addSelectInverted(Function<MenuItemBuilder, MenuItemBuilder> builder) {
            return thenCon("select-inverted", c -> {
                c.getItems().add(builder.apply(defaultSelectInvert()).build());
            });
        }

        public SelectMenuBuilder addSelectNone(Function<MenuItemBuilder, MenuItemBuilder> builder) {
            return thenCon("select-none", c -> {
                c.getItems().add(builder.apply(defaultSelectNone()).build());
            });
        }

        protected static void selectInverted(MultipleSelectionModel sm) {
            ArrayDeque<Integer> array = new ArrayDeque<>(sm.getSelectedIndices());
            sm.selectAll();
            array.forEach(sm::clearSelection);
        }

    }

    public static void simpleMenuBindingWrap(Menu menu, Function<MenuItem, BooleanProperty> func, boolean some) {
        final ArrayDeque<BooleanProperty> list = new ArrayDeque<>();
        menu.getItems().forEach(item -> {
            if (item instanceof Menu) {
                simpleMenuBindingWrap((Menu) item, func, some);
            }
            list.add(func.apply(item));
        });
        if (list.isEmpty()) {
            return;
        }
        if (list.size() == 1) {
            func.apply(menu).bind(list.pollFirst());
        } else if (list.size() > 1) {
            BooleanExpression bind = list.pollFirst();
            for (BooleanProperty b : list) {
                if (some) {
                    bind = Bindings.or(bind, b);
                } else {
                    bind = Bindings.and(bind, b);
                }

            }
            func.apply(menu).bind(bind);
        }
    }

    public static void simpleMenuBindingWrap(ContextMenu menu, Function<MenuItem, BooleanProperty> func, boolean some) {
        menu.getItems().forEach(item -> {
            if (item instanceof Menu) {
                simpleMenuBindingWrap((Menu) item, func, some);
            }
        });
    }

}
