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
import lt.lb.commons.SafeOpt;
import lt.lb.commons.datasync.base.NodeValid;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.parsing.StringOp;

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
        valid.isValid = t -> StringOp.isNotBlank(t);
        return valid;
    }

    public static <N extends Node> FXValid<String, N> valSimplePath() {
        FXValid<String, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must be a simple path";
        valid.isValid = validatorSimplePath();
        return valid;
    }

    public static <N extends Node> FXValid<String, N> valDirPath() {
        FXValid<String, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must be a directory path";
        valid.isValid = validatorDirPath();
        return valid;
    }

    public static <T,N extends Node> FXValid<Collection<T>, N> valEnsureSelection() {
        FXValid<Collection<T>, N> valid = new FXValid<>();
        valid.errorSupl = t -> "Must make a selection";
        valid.isValid = t -> !t.isEmpty();
        return valid;
    }

    public static Predicate<String> validatorSimplePath() {
        return p -> SafeOpt.ofNullable(p).filter(StringOp::isAlphanumeric).map(Paths::get).filter(Files::notExists).isPresent();
    }

    public static Predicate<String> validatorDirPath() {
        return p -> SafeOpt.ofNullable(p).map(Paths::get).filter(Files::isDirectory).isPresent();
    }
    
    public static <T> List<T> newList(Collection<T> col, T... removed){
        ArrayList<T> list = new ArrayList<>(col);
        for(T t:removed){
            list.remove(t);
        }
        return list;
    }

    public static <T> Predicate<String> validatorUnique(boolean in, boolean ignoreCase, Collection<T> list, Function<? super T, String> func) {
        return validatorUnique(in, list, func, (o1, o2) -> {
            if (ignoreCase) {
                return StringOp.equalsIgnoreCase(o1, o2);
            } else {
                return StringOp.equals(o1, o2);
            }
        });
    }

    public static <T, E> Predicate<E> validatorUnique(boolean in, Collection<T> list, Function<? super T, ? extends E> func, Equator<E> eq) {
        if (in) {
            return selected -> list.stream().map(func).anyMatch(entry -> eq.equals(entry, selected));

        } else {
            return selected -> list.stream().map(func).noneMatch(entry -> eq.equals(entry, selected));
        }
    }
}
