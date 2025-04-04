package lt.lb.commons.io;

import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * @author laim0nas100
 */
public class CopyOptions {

    private final Set<CopyOption> options;

    public CopyOptions() {
        this.options = new LinkedHashSet<>();
    }

    public CopyOptions(Collection<CopyOption> options) {
        this.options = new LinkedHashSet<>(options);
    }

    public CopyOptions(Collection<CopyOption> options, CopyOption option) {
        this.options = new LinkedHashSet<>(options);
        this.options.add(option);
    }

    public CopyOptions with(CopyOption option) {
        return new CopyOptions(options, option);
    }

    public CopyOptions without(CopyOption option) {
        return new CopyOptions(MakeStream.from(options).without(option).toLinkedSet());
    }

    public List<CopyOption> toList() {
        return new ArrayList<>(options);
    }

    public CopyOption[] toArray() {
        return options.stream().toArray(s -> new CopyOption[s]);
    }

    public boolean useStreams() {
        return options.contains(StandardCopyOption.ATOMIC_MOVE);
    }
}
