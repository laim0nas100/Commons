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
