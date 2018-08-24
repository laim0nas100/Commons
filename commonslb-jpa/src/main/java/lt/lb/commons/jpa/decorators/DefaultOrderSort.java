/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.decorators;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class DefaultOrderSort implements OrderSort {

    public boolean ascending = true;
    public boolean nullFirst = false;
    public boolean nullable = true;
    public int queueOrder;

    public Path path;

    public interface LimitResolver<T> {

        public T getLimit(T value, boolean lower);
    }

    public static LimitResolver<Date> dateResolver = (Date value, boolean lower) -> {
        if (lower) {
            return new Date(value.getTime() - 1);
        } else {
            return new Date(value.getTime() + 1);
        }
    };

    public static LimitResolver<Number> numberResolver = (Number value, boolean lower) -> {
        Double dv = value.doubleValue();
        if (lower) {
            dv -= 1;
        } else {
            dv += 1;
        }
//        Class<? extends Number> aClass = value.getClass();
//        if (aClass.isAssignableFrom(Long.class)) {
//            return dv.longValue();
//        } else if (aClass.isAssignableFrom(Integer.class)) {
//            return dv.intValue();
//        } else if (aClass.isAssignableFrom(Short.class)) {
//            return dv.shortValue();
//        } else if (aClass.isAssignableFrom(Byte.class)) {
//            return dv.byteValue();
//        } else if (aClass.isAssignableFrom(Double.class)) {
//            return dv;
//        } else if (aClass.isAssignableFrom(Float.class)) {
//            return dv.floatValue();
//        }

        return dv;
    };

    public static HashMap<Class, LimitResolver> getLimits() {
        HashMap<Class, LimitResolver> m = new HashMap<>();

        m.put(Date.class, dateResolver);
        m.put(Number.class, numberResolver);

        m.put(String.class, (LimitResolver<String>) (String value, boolean lower) -> {
            if (lower) {
                if (!value.isEmpty()) {
                    return value.substring(0, value.length() - 1);
                }
                return value;
            } else {
                return value + "Z";
            }
        });

        return m;
    }

    public static Object resolveValueLimit(Object original, boolean min) {
        Class c = original.getClass();
        HashMap<Class, LimitResolver> limits = getLimits();
        for(Class clz:limits.keySet()){
            if(clz.isAssignableFrom(c)){
                return limits.get(clz).getLimit(original, min);
            }
        }
        throw new IllegalArgumentException(c + " type is not supported by DefaultOrderSort");
    }

    @Override
    public Order construct(EntityManager em, CriteriaQuery query, CriteriaBuilder cb) {

        Expression exp = path;
        if (this.nullable) {
            if (this.needsMin()) {
                exp = cb.min(path);
            } else {
                exp = cb.max(path);
            }
            Selection selection = query.getSelection();//save selection
            query.select(exp);
            Object substitute = resolveValueLimit(em.createQuery(query).getSingleResult(), this.needsMin());
            query.select(selection);//restore selection

            Expression coalesce = cb.coalesce(path, substitute);
            exp = coalesce;
        }

        if (ascending) {
            return cb.asc(exp);
        } else {
            return cb.desc(exp);
        }
    }

    @Override
    public boolean isAscending() {
        return this.ascending;
    }

    @Override
    public boolean isNullFirst() {
        return this.nullFirst;
    }

    @Override
    public boolean isNullable() {
        return this.nullable;
    }

    @Override
    public int getQueueOrder() {
        return this.queueOrder;
    }

    @Override
    public String toString() {
        return "nullable:" + this.isNullable() + " null first" + this.isNullFirst() + " ascending:" + this.isAscending() + " queue order:" + this.getQueueOrder();
    }
}
