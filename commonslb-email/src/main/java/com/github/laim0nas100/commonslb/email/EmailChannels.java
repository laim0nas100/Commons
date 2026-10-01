package com.github.laim0nas100.commonslb.email;

import jakarta.mail.internet.MimeMessage;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public class EmailChannels {

    public Consumer<MimeMessage> inputChannel = (m) -> {
    };
    public Consumer<Throwable> errorChannel = (e) -> {
    };
}
