/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public abstract class IMAPOrPOP3Props extends SearchableEmailProps {

    public abstract String getStore();
    private int folderOpenMode = Folder.READ_ONLY;
    public String folderName = "INBOX";

    public boolean deleteAfterRead = false;

    public int getFolderOpenMode() {
        return this.folderOpenMode;
    }

    public void setFolderOpenMode(int openMode) {
        if (ArrayOp.any((i) -> i == openMode, Folder.READ_ONLY, Folder.READ_WRITE)) {
            folderOpenMode = openMode;
        } else {
            throw new IllegalArgumentException("Folder mode " + openMode + " not supported");
        }
    }

    public List<Consumer<Message>> getAfterReadActions() {
        List<Consumer<Message>> list = new ArrayList();
        list.add((m) -> {
            if (this.getFolderOpenMode() != Folder.READ_WRITE) {
                return;
            }
            F.unsafeRun(() -> {
                m.setFlag(Flags.Flag.DELETED, this.deleteAfterRead);
            });
        });
        return list;
    }
}
