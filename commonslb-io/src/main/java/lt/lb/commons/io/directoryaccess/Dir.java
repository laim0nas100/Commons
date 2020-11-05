package lt.lb.commons.io.directoryaccess;


/**
 *
 * @author laim0nas100
 */
public class Dir extends Fil {

    public Iterable<Fil> files;

    public Iterable<Fil> getFiles() {
        return files;
    }

    public Dir(String absolutePath) {
        super(absolutePath);
    }

}
