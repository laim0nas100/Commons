package lt.lb.commons.javafx.fxrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.datasync.base.NodeValid;
import lt.lb.commons.Equator;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.parsing.StringParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 *
 * @author laim0nas100
 */
public class FXValid<T, N extends Node> extends NodeValid<T, N> {

    public Tooltip tooltip = new Tooltip();

    public FXValid() {
    }

    public FXValid(List<N> nodes) {
        this.referenceSupl = () -> nodes;
    }

    private void toggleTooltip(boolean toggleOn, T managed) {
        if (!toggleOn) {
            tooltip.hide();
            return;
        }
        tooltip.setText(errorSupl.apply(managed));

        if (tooltip.isShowing()) {
            return;
        }
        List<N> references = referenceSupl.get();
        for (N reference : references) {
            Point2D p = reference.localToScene(0.0, 0.0);
            Scene scene = reference.getScene();

            tooltip.show(
                    scene.getWindow(),
                    p.getX() + scene.getX() + scene.getWindow().getX(),
                    p.getY() + scene.getY() + scene.getWindow().getY() + 25
            );
        }

    }

    @Override
    public void showInvalidation(T from) {
        toggleTooltip(true, from);
    }

    @Override
    public void clearInvalidation(T from) {
        toggleTooltip(false, from);
    }

    public static <N extends Node> FXValid<String, N> valNotBlank() {
        FXValid<String, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must not be blank";
        valid.isValid = t -> StringUtils.isNotBlank(t);
        return valid;
    }

    public static <N extends Node> FXValid<String, N> valSimplePath(boolean multiple) {
        FXValid<String, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must be a simple path";
        valid.isValid = validatorSimplePath(multiple);
        return valid;
    }

    public static <N extends Node> FXValid<String, N> valDirPath(boolean multiple) {
        FXValid<String, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must be a directory path";
        valid.isValid = validatorDirPath(multiple);
        return valid;
    }

    public static <T, N extends Node> FXValid<Collection<T>, N> valEnsureSelection() {
        FXValid<Collection<T>, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must make a selection";
        valid.isValid = t -> !t.isEmpty();
        return valid;
    }

    public static Predicate<String> validatorSimplePath(boolean multiple) {
        return validatorPath(multiple, false);
    }

    public static Predicate<String> validatorDirPath(boolean multiple) {
        return validatorPath(multiple, true);
    }

    public static Predicate<String> validatorPath(boolean multiple, boolean dir) {
        return p -> SafeOpt.ofNullable(p)
                .map(path -> {
                    if (multiple && Strings.CS.contains(path, "\n")) {
                        return MakeStream.from(StringParser.split(path, "\n"));
                    }
                    return MakeStream.fromValues(path);
                })
                .map(stream -> stream.map(String::trim).map(Paths::get))
                .map(stream -> {
                    if (dir) {
                        return stream.allMatch(Files::isDirectory);
                    } else {
                        return stream.allMatch(Files::exists);
                    }
                })
                .orElse(false);
    }

    public static <T> List<T> newList(Collection<T> col, T... removed) {
        ArrayList<T> list = new ArrayList<>(col);
        for (T t : removed) {
            list.remove(t);
        }
        return list;
    }

    public static <T> Predicate<String> validatorUnique(boolean in, boolean ignoreCase, Collection<T> list, Function<? super T, String> func) {
        return validatorUnique(in, list, func, (o1, o2) -> {
            if (ignoreCase) {
                return StringUtils.equalsIgnoreCase(o1, o2);
            } else {
                return StringUtils.equals(o1, o2);
            }
        });
    }

    public static <T, E> Predicate<E> validatorUnique(boolean in, Collection<T> list, Function<? super T, ? extends E> func, Equator<E> eq) {
        if (in) {
            return selected -> list.stream().map(func).anyMatch(entry -> eq.equate(entry, selected));

        } else {
            return selected -> list.stream().map(func).noneMatch(entry -> eq.equate(entry, selected));
        }
    }
}
