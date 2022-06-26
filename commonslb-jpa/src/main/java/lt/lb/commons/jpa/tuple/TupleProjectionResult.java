package lt.lb.commons.jpa.tuple;

import java.util.List;
import lt.lb.commons.containers.collections.ImmutableCollections;

/**
 *
 * @author laim0nas100
 */
public class TupleProjectionResult<R,T> implements TupleProjectionResultList<R,T> {

    private List<T> list;

    protected void create(T... objects) {
        this.list = ImmutableCollections.listOf(objects);
    }

    public TupleProjectionResult() {
        create();
    }

    public TupleProjectionResult(T v0) {
        create(v0);
    }

    public TupleProjectionResult(T v0, T v1) {
        create(v0, v1);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2
    ) {
        create(v0, v1, v2);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2,
            T v3
    ) {
        create(v0, v1, v2, v3);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4
    ) {
        create(v0, v1, v2, v3, v4);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2,
            T v3,
            T v4,
            T v5
    ) {
        create(v0, v1, v2, v3, v4, v5);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6
    ) {
        create(v0, v1, v2, v3, v4, v5, v6);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10,
            T v11
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10,
            T v11,
            T v12
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10,
            T v11,
            T v12,
            T v13
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    public TupleProjectionResult(
            T v0,
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10,
            T v11,
            T v12,
            T v13,
            T v14
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    public TupleProjectionResult(
            T v0, 
            T v1,
            T v2,
            T v3,
            T v4,
            T v5,
            T v6,
            T v7,
            T v8,
            T v9,
            T v10,
            T v11,
            T v12,
            T v13,
            T v14,
            T v15
    ) {
        create(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    @Override
    public List getList() {
        return list;
    }

    @Override
    public String toString() {
        return list.toString();
    }

}
