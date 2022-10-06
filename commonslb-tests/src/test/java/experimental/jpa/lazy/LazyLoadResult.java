package experimental.jpa.lazy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 */
public class LazyLoadResult<ID> {
    
    private final Long size;
    private final LinkedHashMap<ID, List<ID>> grouped;
    private final LinkedHashMap<ID, Long> groupedCount;
    private final List<ID> ids;
    private final LazyLoadType type;

    public static final LazyLoadResult<?> empty = new LazyLoadResult(false);
    public static final LazyLoadResult<?> emptyCount = new LazyLoadResult(0L);
    public static final LazyLoadResult<?> emptyList = new LazyLoadResult(ImmutableCollections.listOf());
    public static final LazyLoadResult<?> emptyGrouped = new LazyLoadResult(0L, new LinkedHashMap<Object, List>());
    public static final LazyLoadResult<?> emptyGroupedCount = new LazyLoadResult(0L, new LinkedHashMap<Object, Long>(), null);
    
    public static <ID> LazyLoadResult<ID> emptyList(){
        return F.cast(emptyList);
    }

    public static <ID> LazyLoadResult exists(boolean b) {
        return new LazyLoadResult<ID>(b);
    }

    public static <ID> LazyLoadResult<ID> count(long size) {
        return new LazyLoadResult<>(size);
    }

    public static <ID> LazyLoadResult<ID> ids(List<ID> ids) {
        return new LazyLoadResult<>(ids);
    }

    public static <ID> LazyLoadResult<ID> grouped(LinkedHashMap<ID, List<ID>> ids) {
        return new LazyLoadResult<>((long) ids.size(), ids);
    }

    public static <ID> LazyLoadResult<ID> groupedCount(LinkedHashMap<ID, Long> counts) {
        return new LazyLoadResult<>((long) counts.size(), counts, null);
    }

    private LazyLoadResult(boolean exists) {
        this.size = exists ? 1L : 0L;
        this.type = LazyLoadType.EXISTS;
        this.grouped = null;
        this.groupedCount = null;
        this.ids = null;
    }

    private LazyLoadResult(Long size) {
        this.size = size;
        this.type = LazyLoadType.COUNT;
        this.grouped = null;
        this.groupedCount = null;
        this.ids = null;
    }

    private LazyLoadResult(List<ID> ids) {
        this.ids = ids;
        this.type = LazyLoadType.IDS;
        this.size = (long) ids.size();
        this.grouped = null;
        this.groupedCount = null;
    }

    private LazyLoadResult(Long size, LinkedHashMap<ID, List<ID>> ids) {
        this.grouped = ids;
        this.size = size;
        this.type = LazyLoadType.GROUPED;
        this.groupedCount = null;
        this.ids = null;
    }

    private LazyLoadResult(Long size, LinkedHashMap<ID, Long> ids, Object dummy) {
        this.groupedCount = ids;
        this.size = size;
        this.type = LazyLoadType.GROUPED_COUNT;
        this.grouped = null;
        this.ids = null;
    }

    public boolean exists() {
        return getSize() != 0L;
    }

    public Long getSize() {
        return size;
    }

    public LazyLoadType getType() {
        return type;
    }

    public List<ID> getIds() {
        if (ids == null) {
            if (grouped != null) {
                return new ArrayList<>(grouped.keySet());
            }
            if (groupedCount != null) {
                return new ArrayList<>(groupedCount.keySet());
            }
            return ImmutableCollections.listOf();
        }
        return this.ids;
    }

    public int size() {
        return size.intValue();
    }

    public LinkedHashMap<ID, List<ID>> getGrouped() {
        if (grouped == null) {
            return new LinkedHashMap<>();
        }
        return grouped;
    }

    public LinkedHashMap<ID, Long> getGroupedCount() {
        if (groupedCount == null) {
            return new LinkedHashMap<>();
        }
        return groupedCount;
    }


    public LazyLoadResult<ID> subresult(int from, int to) {
        switch (type) {
            case IDS:
                return LazyLoadResult.ids(ids.subList(from, to));

            case GROUPED:
                LinkedHashMap<ID, List<ID>> newMap = new LinkedHashMap<>(to - from);
                For.entries().withInterval(from, to).iterate(grouped, (k, list) -> {
                    newMap.put(k, list);
                });

                return LazyLoadResult.grouped(newMap);

            case GROUPED_COUNT:
                LinkedHashMap<ID, Long> newCountMap = new LinkedHashMap<>(to - from);
                For.entries().withInterval(from, to).iterate(groupedCount, (k, count) -> {
                    newCountMap.put(k, count);
                });

                return LazyLoadResult.groupedCount(newCountMap);

            default:
                throw new IllegalStateException("Unsupported subdivision for type " + type);
        }
    }
}
