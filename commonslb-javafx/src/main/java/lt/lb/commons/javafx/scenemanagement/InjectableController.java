/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * Minimal FX controller with injectable frame information
 * @author laim0nas100
 */
public interface InjectableController extends BaseController{
    
    public void inject(Frame frame, URL url, ResourceBundle rb);
    

}
