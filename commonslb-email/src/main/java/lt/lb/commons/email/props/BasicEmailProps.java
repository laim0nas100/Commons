/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

import java.util.Properties;

/**
 *
 * @author laim0nas100
 */
public abstract class BasicEmailProps extends Properties {

    public String username;
    public String password;
    public String host;
    public int port;
    
    public abstract void populate();
}
