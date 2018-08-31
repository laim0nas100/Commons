/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.email.props;

import javax.mail.Folder;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public abstract class IMAPOrPOP3Props extends SearchableEmailProps {

    public abstract String getStore();
    private int folderOpenMode = Folder.READ_ONLY;
    public String folderName = "INBOX";

    public int getFolderOpenMode() {
        return this.folderOpenMode;
    }

    public void setFolderOpenMode(int openMode) {
        if (ArrayOp.any((i)-> i == openMode, Folder.READ_ONLY, Folder.READ_WRITE)) {
            folderOpenMode = openMode;
        } else {
            throw new IllegalArgumentException("Folder mode " + openMode + " not supported");
        }
    }

}
