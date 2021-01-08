package lt.lb.commons.email;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author laim0nas100
 */
public class ExposedEmailConnection {

    public CompletableFuture<Session> emailSession = new CompletableFuture();
    public CompletableFuture<Store> emailStore = new CompletableFuture();
    public CompletableFuture<Folder> emailFolder = new CompletableFuture();

}
