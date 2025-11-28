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

    private final boolean useStreams;
    private final Set<CopyOption> options;

    public CopyOptions() {
        this.options = new LinkedHashSet<>();
        this.useStreams = false;
    }

    public CopyOptions(Collection<CopyOption> options, boolean useStreams) {
        this.options = new LinkedHashSet<>(options);
        this.useStreams = useStreams;
    }

    public CopyOptions(Collection<CopyOption> options, CopyOption option, boolean useStreams) {
        this.options = new LinkedHashSet<>(options);
        this.options.add(option);
        this.useStreams = useStreams;
    }

    public CopyOptions with(CopyOption option) {
        if (options.contains(option)) {
            //no change
            return this;
        }
        return new CopyOptions(options, option, useStreams);
    }

    public CopyOptions without(CopyOption option) {
        if (options.contains(option)) {
            return new CopyOptions(MakeStream.from(options).without(option).toLinkedSet(), useStreams);
        }
        //no change
        return this;
    }

    private CopyOptions usingStreams(boolean use) {
        if (this.useStreams == use) {
            //no change
            return this;
        }
        return new CopyOptions(options, use);
    }

    public CopyOptions withStreams() {
        return usingStreams(true);
    }

    public CopyOptions withoutStreams() {
        return usingStreams(false);
    }

    public List<CopyOption> toList() {
        return new ArrayList<>(options);
    }

    public CopyOption[] toArray() {
        return options.stream().toArray(s -> new CopyOption[s]);
    }

    public boolean isUsingStreams() {
        return useStreams;
    }

    public boolean isAtomicMove() {
        return options.contains(StandardCopyOption.ATOMIC_MOVE);
    }

    public boolean isReplaceExisting() {
        return options.contains(StandardCopyOption.REPLACE_EXISTING);
    }

    public boolean isCopyAttributes() {
        return options.contains(StandardCopyOption.COPY_ATTRIBUTES);
    }
}
