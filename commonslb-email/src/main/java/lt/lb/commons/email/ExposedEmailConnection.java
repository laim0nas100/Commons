package lt.lb.commons.email;

import jakarta.mail.Folder;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author laim0nas100
 */
public class ExposedEmailConnection {

    public CompletableFuture<Session> emailSession = new CompletableFuture();
    public CompletableFuture<Store> emailStore = new CompletableFuture();
    public CompletableFuture<Folder> emailFolder = new CompletableFuture();

}
