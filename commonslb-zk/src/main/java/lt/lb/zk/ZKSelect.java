package lt.lb.zk;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.uncheckedutils.SafeOpt;
import org.zkoss.zk.ui.Component;

/**
 *
 * @author laim0nas100
 */
public class ZKSelect {

    private static TreeVisitor<Component> componentVisitor(Predicate<Component> filter, Predicate<Component> cons) {
        return new TreeVisitor<Component>() {
            @Override
            public Boolean find(Component item) {
                if (filter.test(item)) {
                    return cons.test(item);
                }
                return false;
            }

            @Override
            public Iterable<Component> getChildren(Component item) {
                return SafeOpt.ofNullable(item).map(m -> m.getChildren()).orElse(ImmutableCollections.listOf());
            }
        };
    }

    private static TreeVisitor<Component> componentVisitor(Predicate<Component> filter, Consumer<Component> cons) {
        return componentVisitor(filter, c -> {
            cons.accept(c);
            return false;
        });
    }

    public static void iterateChildren(Predicate<Component> filter, Component root, Consumer<Component> f) {
        componentVisitor(filter, f).BFS(root);
    }

    public static Set<Component> selectAll(Predicate<Component> condition, Component root) {
        HashSet<Component> collected = new HashSet<>();
        Predicate<Component> collector = t -> {
            collected.add(t);
            return false;
        };
        componentVisitor(condition, collector).BFS(root, new HashSet<>());
        return collected;
    }

    public static Optional<Component> selectFirst(Predicate<Component> condition, Component root) {
        return componentVisitor(t -> true, condition).BFS(root, new HashSet<>());
    }

    public static Optional<Component> selectFirstParent(Predicate<Component> condition, Component child) {
        if (condition.test(child)) {
            return Optional.ofNullable(child);
        } else {
            if (child.getParent() != null) {
                return selectFirstParent(condition, child.getParent());
            } else {
                return Optional.empty();
            }
        }
    }

    public static <R> Optional<R> getComposer(Class<R> cls, Component comp) {
        Object attribute = comp.getAttribute("$composer");

        if (Ins.instanceOf(attribute, cls)) {
            return Optional.of((R) attribute);
        }

        return comp.getAttributes().values().stream()
                .filter(v -> Ins.instanceOf(v, cls))
                .map(m -> (R) m).findFirst();

    }
}
