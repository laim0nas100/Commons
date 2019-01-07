/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email;

import java.util.function.Consumer;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author laim0nas100
 */
public class EmailChannels {

    public Consumer<MimeMessage> inputChannel = (m)-> {};
    public Consumer<Throwable> errorChannel = (e)->{};
}
