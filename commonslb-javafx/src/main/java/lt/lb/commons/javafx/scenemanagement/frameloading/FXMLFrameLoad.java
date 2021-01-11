package lt.lb.commons.javafx.scenemanagement.frameloading;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.javafx.scenemanagement.BaseController;
import lt.lb.commons.javafx.scenemanagement.FXMLFrame;
import lt.lb.commons.javafx.scenemanagement.FrameManager;

/**
 *
 * @author laim0nas100
 */
public class FXMLFrameLoad<T extends BaseController> extends BaseFrameLoad<FXMLFrame<T>> {

    public FXMLFrameLoad(URL resource) {
        this(resource, null);
    }

    public FXMLFrameLoad(URL resource, ResourceBundle bundle) {
        this.resource = Objects.requireNonNull(resource);
        this.bundle = bundle;
    }

    public static <T extends BaseController> FXMLFrameLoad<T> of(URL resource) {
        return new FXMLFrameLoad(resource);
    }

    public static <T extends BaseController> FXMLFrameLoad of(URL resource, ResourceBundle bundle) {
        return new FXMLFrameLoad(resource, bundle);
    }

    protected URL resource;
    protected ResourceBundle bundle;
    protected T controller;

    @Override
    protected Parent generateRoot() throws IOException {
        FXMLLoader loader = new FXMLLoader(resource, bundle);
        Parent load = loader.load();
        controller = loader.getController();
        return load;
    }

    @Override
    protected FXMLFrame generateFrame(FrameManager manager, String ID, String type) throws Exception {
        return new FXMLFrame(manager, getStage(), getController(), getResource(), type, ID);
    }

    public T getController() throws Exception {
        if (controller == null) {
            getRoot();//loads
        }
        return Objects.requireNonNull(controller, "Failed to load controller");
    }

    public SafeOpt<T> getControllerSafe() {
        return SafeOpt.of(this).map(m -> m.getController());
    }

    public URL getResource() {
        return resource;
    }

    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    @Override
    public void reset() {
        super.reset();
        controller = null;
    }

    @Override
    public void decorateAfter() throws Exception {
        super.decorateAfter(); //To change body of generated methods, choose Tools | Templates.
    }

}
