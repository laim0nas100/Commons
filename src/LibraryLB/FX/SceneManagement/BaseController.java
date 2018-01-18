/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.FX.SceneManagement;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 *
 * @author Laimonas Beniušis
 */

public abstract class BaseController implements Initializable{
    public Frame frame;
            
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    } 
    public abstract void exit();
    public abstract void update();
    public abstract void initialize();
}
