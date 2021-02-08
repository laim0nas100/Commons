package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * Minimal FX controller with injectable frame information
 *
 * @author laim0nas100
 */
public interface InjectableController<T extends InjectableController> extends BaseController<T> {

    public void inject(Frame frame, URL url, ResourceBundle rb);

    public Frame getFrame();
    
    public default String getFrameID(){
        return getFrame().getID();
    }
    
    public default String getFrameType(){
        return getFrame().getType();
    }
    
    public default FrameManager getFrameManager(){
        return getFrame().getManager();
    }

    /**
     * Default implementation calls close on the MultiStageManager, calls exit
     * on frame that is being closed (via onCloseRequest). This way closing
     * logic is unified, from calling this method, or pressing X on the window.
     */
    @Override
    public default void close() {
        getFrame().close();
    }

    @Override
    public default void show() {
        getFrame().show();
    }

    @Override
    public default void hide() {
        getFrame().hide();
    }
    
    

}
