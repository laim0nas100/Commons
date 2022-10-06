package experimental.jpa.lazy;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class LazyLoadContext {

    private long pageIndex;
    private int pageSize;
    private final LazyLoadType type;

    private LazyLoadContext(LazyLoadType type) {
        this.type = Objects.requireNonNull(type);
    }

    public static LazyLoadContext countOrIds() {
        return new LazyLoadContext(LazyLoadType.OPTIMAL);
    }

    public static LazyLoadContext count() {
        return new LazyLoadContext(LazyLoadType.COUNT);
    }

    public static LazyLoadContext exists() {
        return new LazyLoadContext(LazyLoadType.EXISTS);
    }

    public static LazyLoadContext list(int pageIndex, int pageSize) {
        return list((long) pageIndex, pageSize);
    }

    public static LazyLoadContext list(long pageIndex, int pageSize) {
        LazyLoadContext ctx = new LazyLoadContext(LazyLoadType.IDS);
        ctx.setPageIndex(pageIndex);
        ctx.setPageSize(pageSize);
        return ctx;
    }

    public LazyLoadType getType() {
        return type;
    }

    public boolean isLoadList() {
        return type == LazyLoadType.IDS;
    }

    public Long getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(Long pageIndex) {
        this.pageIndex = pageIndex;
    }

    public boolean isOptimal() {
        return type == LazyLoadType.OPTIMAL;
    }

    public boolean isLoadCount() {
        return type == LazyLoadType.COUNT;
    }

    public boolean isLoadExists() {
        return type == LazyLoadType.EXISTS;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
