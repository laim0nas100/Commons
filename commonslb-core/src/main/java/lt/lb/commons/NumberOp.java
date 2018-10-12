/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 *
 * +,-,*,/,% Generic number arithmetic operations with explicit number casting
 *
 * @author laim0nas100
 */
public class NumberOp {

    /**
     * Generic number add operation
     *
     * @param <T> dominant and return type
     * @param <K> secondary type
     * @param a
     * @param b
     * @return
     * @throws ClassCastException (if number conversion fails) i.e 1 + 1.5
     */
    public static <T extends Number, K extends Number> T add(T a, K b) {

        // Integer
        if (a instanceof Integer && b instanceof Integer) {
            return F.cast(a.intValue() + b.intValue());
        }
        if (a instanceof Integer && b instanceof Long) {
            return F.cast(a.intValue() + b.longValue());
        }
        if (a instanceof Integer && b instanceof Short) {
            return F.cast(a.intValue() + b.shortValue());
        }
        if (a instanceof Integer && b instanceof Byte) {
            return F.cast(a.intValue() + b.byteValue());
        }
        if (a instanceof Integer && b instanceof Float) {
            return F.cast(a.intValue() + b.floatValue());
        }
        if (a instanceof Integer && b instanceof Double) {
            return F.cast(a.intValue() + b.doubleValue());
        }

        //Long
        if (a instanceof Long && b instanceof Integer) {
            return F.cast(a.longValue() + b.intValue());
        }
        if (a instanceof Long && b instanceof Long) {
            return F.cast(a.longValue() + b.longValue());
        }
        if (a instanceof Long && b instanceof Short) {
            return F.cast(a.longValue() + b.shortValue());
        }
        if (a instanceof Long && b instanceof Byte) {
            return F.cast(a.longValue() + b.byteValue());
        }
        if (a instanceof Long && b instanceof Float) {
            return F.cast(a.longValue() + b.floatValue());
        }
        if (a instanceof Long && b instanceof Double) {
            return F.cast(a.longValue() + b.doubleValue());
        }

        //Double
        if (a instanceof Double && b instanceof Integer) {
            return F.cast(a.doubleValue() + b.intValue());
        }
        if (a instanceof Double && b instanceof Long) {
            return F.cast(a.doubleValue() + b.longValue());
        }
        if (a instanceof Double && b instanceof Short) {
            return F.cast(a.doubleValue() + b.shortValue());
        }
        if (a instanceof Double && b instanceof Byte) {
            return F.cast(a.doubleValue() + b.byteValue());
        }
        if (a instanceof Double && b instanceof Float) {
            return F.cast(a.doubleValue() + b.floatValue());
        }
        if (a instanceof Double && b instanceof Double) {
            return F.cast(a.doubleValue() + b.doubleValue());
        }

        //Float
        if (a instanceof Float && b instanceof Integer) {
            return F.cast(a.floatValue() + b.intValue());
        }
        if (a instanceof Float && b instanceof Long) {
            return F.cast(a.floatValue() + b.longValue());
        }
        if (a instanceof Float && b instanceof Short) {
            return F.cast(a.floatValue() + b.shortValue());
        }
        if (a instanceof Float && b instanceof Byte) {
            return F.cast(a.floatValue() + b.byteValue());
        }
        if (a instanceof Float && b instanceof Float) {
            return F.cast(a.floatValue() + b.floatValue());
        }
        if (a instanceof Float && b instanceof Double) {
            return F.cast(a.floatValue() + b.doubleValue());
        }

        //Short
        if (a instanceof Short && b instanceof Integer) {
            return F.cast(a.shortValue() + b.intValue());
        }
        if (a instanceof Short && b instanceof Long) {
            return F.cast(a.shortValue() + b.longValue());
        }
        if (a instanceof Short && b instanceof Short) {
            return F.cast(a.shortValue() + b.shortValue());
        }
        if (a instanceof Short && b instanceof Byte) {
            return F.cast(a.shortValue() + b.byteValue());
        }
        if (a instanceof Short && b instanceof Float) {
            return F.cast(a.shortValue() + b.floatValue());
        }
        if (a instanceof Short && b instanceof Double) {
            return F.cast(a.shortValue() + b.doubleValue());
        }

        //Byte
        if (a instanceof Byte && b instanceof Integer) {
            return F.cast(a.byteValue() + b.intValue());
        }
        if (a instanceof Byte && b instanceof Long) {
            return F.cast(a.byteValue() + b.longValue());
        }
        if (a instanceof Byte && b instanceof Short) {
            return F.cast(a.byteValue() + b.shortValue());
        }
        if (a instanceof Byte && b instanceof Byte) {
            return F.cast(a.byteValue() + b.byteValue());
        }
        if (a instanceof Byte && b instanceof Float) {
            return F.cast(a.byteValue() + b.floatValue());
        }
        if (a instanceof Byte && b instanceof Double) {
            return F.cast(a.byteValue() + b.doubleValue());
        }

        throw new UnsupportedOperationException("Not supported number format???");

    }

    /**
     * Generic number subtract operation
     *
     * @param <T> dominant and return type
     * @param <K> secondary type
     * @param a
     * @param b
     * @return
     * @throws ClassCastException (if number conversion fails) i.e 2 - 1.5
     */
    public static <T extends Number, K extends Number> T subtract(T a, K b) {

        // Integer
        if (a instanceof Integer && b instanceof Integer) {
            return F.cast(a.intValue() - b.intValue());
        }
        if (a instanceof Integer && b instanceof Long) {
            return F.cast(a.intValue() - b.longValue());
        }
        if (a instanceof Integer && b instanceof Short) {
            return F.cast(a.intValue() - b.shortValue());
        }
        if (a instanceof Integer && b instanceof Byte) {
            return F.cast(a.intValue() - b.byteValue());
        }
        if (a instanceof Integer && b instanceof Float) {
            return F.cast(a.intValue() - b.floatValue());
        }
        if (a instanceof Integer && b instanceof Double) {
            return F.cast(a.intValue() - b.doubleValue());
        }

        //Long
        if (a instanceof Long && b instanceof Integer) {
            return F.cast(a.longValue() - b.intValue());
        }
        if (a instanceof Long && b instanceof Long) {
            return F.cast(a.longValue() - b.longValue());
        }
        if (a instanceof Long && b instanceof Short) {
            return F.cast(a.longValue() - b.shortValue());
        }
        if (a instanceof Long && b instanceof Byte) {
            return F.cast(a.longValue() - b.byteValue());
        }
        if (a instanceof Long && b instanceof Float) {
            return F.cast(a.longValue() - b.floatValue());
        }
        if (a instanceof Long && b instanceof Double) {
            return F.cast(a.longValue() - b.doubleValue());
        }

        //Double
        if (a instanceof Double && b instanceof Integer) {
            return F.cast(a.doubleValue() - b.intValue());
        }
        if (a instanceof Double && b instanceof Long) {
            return F.cast(a.doubleValue() - b.longValue());
        }
        if (a instanceof Double && b instanceof Short) {
            return F.cast(a.doubleValue() - b.shortValue());
        }
        if (a instanceof Double && b instanceof Byte) {
            return F.cast(a.doubleValue() - b.byteValue());
        }
        if (a instanceof Double && b instanceof Float) {
            return F.cast(a.doubleValue() - b.floatValue());
        }
        if (a instanceof Double && b instanceof Double) {
            return F.cast(a.doubleValue() - b.doubleValue());
        }

        //Float
        if (a instanceof Float && b instanceof Integer) {
            return F.cast(a.floatValue() - b.intValue());
        }
        if (a instanceof Float && b instanceof Long) {
            return F.cast(a.floatValue() - b.longValue());
        }
        if (a instanceof Float && b instanceof Short) {
            return F.cast(a.floatValue() - b.shortValue());
        }
        if (a instanceof Float && b instanceof Byte) {
            return F.cast(a.floatValue() - b.byteValue());
        }
        if (a instanceof Float && b instanceof Float) {
            return F.cast(a.floatValue() - b.floatValue());
        }
        if (a instanceof Float && b instanceof Double) {
            return F.cast(a.floatValue() - b.doubleValue());
        }

        //Short
        if (a instanceof Short && b instanceof Integer) {
            return F.cast(a.shortValue() - b.intValue());
        }
        if (a instanceof Short && b instanceof Long) {
            return F.cast(a.shortValue() - b.longValue());
        }
        if (a instanceof Short && b instanceof Short) {
            return F.cast(a.shortValue() - b.shortValue());
        }
        if (a instanceof Short && b instanceof Byte) {
            return F.cast(a.shortValue() - b.byteValue());
        }
        if (a instanceof Short && b instanceof Float) {
            return F.cast(a.shortValue() - b.floatValue());
        }
        if (a instanceof Short && b instanceof Double) {
            return F.cast(a.shortValue() - b.doubleValue());
        }

        //Byte
        if (a instanceof Byte && b instanceof Integer) {
            return F.cast(a.byteValue() - b.intValue());
        }
        if (a instanceof Byte && b instanceof Long) {
            return F.cast(a.byteValue() - b.longValue());
        }
        if (a instanceof Byte && b instanceof Short) {
            return F.cast(a.byteValue() - b.shortValue());
        }
        if (a instanceof Byte && b instanceof Byte) {
            return F.cast(a.byteValue() - b.byteValue());
        }
        if (a instanceof Byte && b instanceof Float) {
            return F.cast(a.byteValue() - b.floatValue());
        }
        if (a instanceof Byte && b instanceof Double) {
            return F.cast(a.byteValue() - b.doubleValue());
        }

        throw new UnsupportedOperationException("Not supported number format???");

    }

    /**
     * Generic number multiply operation
     *
     * @param <T> dominant and return type
     * @param <K> secondary type
     * @param a
     * @param b
     * @return
     * @throws ClassCastException (if number conversion fails) i.e 2 * 1.5
     */
    public static <T extends Number, K extends Number> T multiply(T a, K b) {

        // Integer
        if (a instanceof Integer && b instanceof Integer) {
            return F.cast(a.intValue() * b.intValue());
        }
        if (a instanceof Integer && b instanceof Long) {
            return F.cast(a.intValue() * b.longValue());
        }
        if (a instanceof Integer && b instanceof Short) {
            return F.cast(a.intValue() * b.shortValue());
        }
        if (a instanceof Integer && b instanceof Byte) {
            return F.cast(a.intValue() * b.byteValue());
        }
        if (a instanceof Integer && b instanceof Float) {
            return F.cast(a.intValue() * b.floatValue());
        }
        if (a instanceof Integer && b instanceof Double) {
            return F.cast(a.intValue() * b.doubleValue());
        }

        //Long
        if (a instanceof Long && b instanceof Integer) {
            return F.cast(a.longValue() * b.intValue());
        }
        if (a instanceof Long && b instanceof Long) {
            return F.cast(a.longValue() * b.longValue());
        }
        if (a instanceof Long && b instanceof Short) {
            return F.cast(a.longValue() * b.shortValue());
        }
        if (a instanceof Long && b instanceof Byte) {
            return F.cast(a.longValue() * b.byteValue());
        }
        if (a instanceof Long && b instanceof Float) {
            return F.cast(a.longValue() * b.floatValue());
        }
        if (a instanceof Long && b instanceof Double) {
            return F.cast(a.longValue() * b.doubleValue());
        }

        //Double
        if (a instanceof Double && b instanceof Integer) {
            return F.cast(a.doubleValue() * b.intValue());
        }
        if (a instanceof Double && b instanceof Long) {
            return F.cast(a.doubleValue() * b.longValue());
        }
        if (a instanceof Double && b instanceof Short) {
            return F.cast(a.doubleValue() * b.shortValue());
        }
        if (a instanceof Double && b instanceof Byte) {
            return F.cast(a.doubleValue() * b.byteValue());
        }
        if (a instanceof Double && b instanceof Float) {
            return F.cast(a.doubleValue() * b.floatValue());
        }
        if (a instanceof Double && b instanceof Double) {
            return F.cast(a.doubleValue() * b.doubleValue());
        }

        //Float
        if (a instanceof Float && b instanceof Integer) {
            return F.cast(a.floatValue() * b.intValue());
        }
        if (a instanceof Float && b instanceof Long) {
            return F.cast(a.floatValue() * b.longValue());
        }
        if (a instanceof Float && b instanceof Short) {
            return F.cast(a.floatValue() * b.shortValue());
        }
        if (a instanceof Float && b instanceof Byte) {
            return F.cast(a.floatValue() * b.byteValue());
        }
        if (a instanceof Float && b instanceof Float) {
            return F.cast(a.floatValue() * b.floatValue());
        }
        if (a instanceof Float && b instanceof Double) {
            return F.cast(a.floatValue() * b.doubleValue());
        }

        //Short
        if (a instanceof Short && b instanceof Integer) {
            return F.cast(a.shortValue() * b.intValue());
        }
        if (a instanceof Short && b instanceof Long) {
            return F.cast(a.shortValue() * b.longValue());
        }
        if (a instanceof Short && b instanceof Short) {
            return F.cast(a.shortValue() * b.shortValue());
        }
        if (a instanceof Short && b instanceof Byte) {
            return F.cast(a.shortValue() * b.byteValue());
        }
        if (a instanceof Short && b instanceof Float) {
            return F.cast(a.shortValue() * b.floatValue());
        }
        if (a instanceof Short && b instanceof Double) {
            return F.cast(a.shortValue() * b.doubleValue());
        }

        //Byte
        if (a instanceof Byte && b instanceof Integer) {
            return F.cast(a.byteValue() * b.intValue());
        }
        if (a instanceof Byte && b instanceof Long) {
            return F.cast(a.byteValue() * b.longValue());
        }
        if (a instanceof Byte && b instanceof Short) {
            return F.cast(a.byteValue() * b.shortValue());
        }
        if (a instanceof Byte && b instanceof Byte) {
            return F.cast(a.byteValue() * b.byteValue());
        }
        if (a instanceof Byte && b instanceof Float) {
            return F.cast(a.byteValue() * b.floatValue());
        }
        if (a instanceof Byte && b instanceof Double) {
            return F.cast(a.byteValue() * b.doubleValue());
        }

        throw new UnsupportedOperationException("Not supported number format???");

    }

    /**
     * Generic number divide operation
     *
     * @param <T> dominant and return type
     * @param <K> secondary type
     * @param a
     * @param b
     * @return
     * @throws ClassCastException (if number conversion fails) i.e 2 / 1.5
     */
    public static <T extends Number, K extends Number> T divide(T a, K b) {

        // Integer
        if (a instanceof Integer && b instanceof Integer) {
            return F.cast(a.intValue() / b.intValue());
        }
        if (a instanceof Integer && b instanceof Long) {
            return F.cast(a.intValue() / b.longValue());
        }
        if (a instanceof Integer && b instanceof Short) {
            return F.cast(a.intValue() / b.shortValue());
        }
        if (a instanceof Integer && b instanceof Byte) {
            return F.cast(a.intValue() / b.byteValue());
        }
        if (a instanceof Integer && b instanceof Float) {
            return F.cast(a.intValue() / b.floatValue());
        }
        if (a instanceof Integer && b instanceof Double) {
            return F.cast(a.intValue() / b.doubleValue());
        }

        //Long
        if (a instanceof Long && b instanceof Integer) {
            return F.cast(a.longValue() / b.intValue());
        }
        if (a instanceof Long && b instanceof Long) {
            return F.cast(a.longValue() / b.longValue());
        }
        if (a instanceof Long && b instanceof Short) {
            return F.cast(a.longValue() / b.shortValue());
        }
        if (a instanceof Long && b instanceof Byte) {
            return F.cast(a.longValue() / b.byteValue());
        }
        if (a instanceof Long && b instanceof Float) {
            return F.cast(a.longValue() / b.floatValue());
        }
        if (a instanceof Long && b instanceof Double) {
            return F.cast(a.longValue() / b.doubleValue());
        }

        //Double
        if (a instanceof Double && b instanceof Integer) {
            return F.cast(a.doubleValue() / b.intValue());
        }
        if (a instanceof Double && b instanceof Long) {
            return F.cast(a.doubleValue() / b.longValue());
        }
        if (a instanceof Double && b instanceof Short) {
            return F.cast(a.doubleValue() / b.shortValue());
        }
        if (a instanceof Double && b instanceof Byte) {
            return F.cast(a.doubleValue() / b.byteValue());
        }
        if (a instanceof Double && b instanceof Float) {
            return F.cast(a.doubleValue() / b.floatValue());
        }
        if (a instanceof Double && b instanceof Double) {
            return F.cast(a.doubleValue() / b.doubleValue());
        }

        //Float
        if (a instanceof Float && b instanceof Integer) {
            return F.cast(a.floatValue() / b.intValue());
        }
        if (a instanceof Float && b instanceof Long) {
            return F.cast(a.floatValue() / b.longValue());
        }
        if (a instanceof Float && b instanceof Short) {
            return F.cast(a.floatValue() / b.shortValue());
        }
        if (a instanceof Float && b instanceof Byte) {
            return F.cast(a.floatValue() / b.byteValue());
        }
        if (a instanceof Float && b instanceof Float) {
            return F.cast(a.floatValue() / b.floatValue());
        }
        if (a instanceof Float && b instanceof Double) {
            return F.cast(a.floatValue() / b.doubleValue());
        }

        //Short
        if (a instanceof Short && b instanceof Integer) {
            return F.cast(a.shortValue() / b.intValue());
        }
        if (a instanceof Short && b instanceof Long) {
            return F.cast(a.shortValue() / b.longValue());
        }
        if (a instanceof Short && b instanceof Short) {
            return F.cast(a.shortValue() / b.shortValue());
        }
        if (a instanceof Short && b instanceof Byte) {
            return F.cast(a.shortValue() / b.byteValue());
        }
        if (a instanceof Short && b instanceof Float) {
            return F.cast(a.shortValue() / b.floatValue());
        }
        if (a instanceof Short && b instanceof Double) {
            return F.cast(a.shortValue() / b.doubleValue());
        }

        //Byte
        if (a instanceof Byte && b instanceof Integer) {
            return F.cast(a.byteValue() / b.intValue());
        }
        if (a instanceof Byte && b instanceof Long) {
            return F.cast(a.byteValue() / b.longValue());
        }
        if (a instanceof Byte && b instanceof Short) {
            return F.cast(a.byteValue() / b.shortValue());
        }
        if (a instanceof Byte && b instanceof Byte) {
            return F.cast(a.byteValue() / b.byteValue());
        }
        if (a instanceof Byte && b instanceof Float) {
            return F.cast(a.byteValue() / b.floatValue());
        }
        if (a instanceof Byte && b instanceof Double) {
            return F.cast(a.byteValue() / b.doubleValue());
        }

        throw new UnsupportedOperationException("Not supported number format???");

    }

    /**
     * Generic number modulus operation
     *
     * @param <T> dominant and return type
     * @param <K> secondary type
     * @param a
     * @param b
     * @return
     * @throws ClassCastException (if number conversion fails) i.e 2 % 1.5
     */
    public static <T extends Number, K extends Number> T modulus(T a, K b) {
        // Integer
        if (a instanceof Integer && b instanceof Integer) {
            return F.cast(a.intValue() % b.intValue());
        }
        if (a instanceof Integer && b instanceof Long) {
            return F.cast(a.intValue() % b.longValue());
        }
        if (a instanceof Integer && b instanceof Short) {
            return F.cast(a.intValue() % b.shortValue());
        }
        if (a instanceof Integer && b instanceof Byte) {
            return F.cast(a.intValue() % b.byteValue());
        }
        if (a instanceof Integer && b instanceof Float) {
            return F.cast(a.intValue() % b.floatValue());
        }
        if (a instanceof Integer && b instanceof Double) {
            return F.cast(a.intValue() % b.doubleValue());
        }

        //Long
        if (a instanceof Long && b instanceof Integer) {
            return F.cast(a.longValue() % b.intValue());
        }
        if (a instanceof Long && b instanceof Long) {
            return F.cast(a.longValue() % b.longValue());
        }
        if (a instanceof Long && b instanceof Short) {
            return F.cast(a.longValue() % b.shortValue());
        }
        if (a instanceof Long && b instanceof Byte) {
            return F.cast(a.longValue() % b.byteValue());
        }
        if (a instanceof Long && b instanceof Float) {
            return F.cast(a.longValue() % b.floatValue());
        }
        if (a instanceof Long && b instanceof Double) {
            return F.cast(a.longValue() % b.doubleValue());
        }

        //Double
        if (a instanceof Double && b instanceof Integer) {
            return F.cast(a.doubleValue() % b.intValue());
        }
        if (a instanceof Double && b instanceof Long) {
            return F.cast(a.doubleValue() % b.longValue());
        }
        if (a instanceof Double && b instanceof Short) {
            return F.cast(a.doubleValue() % b.shortValue());
        }
        if (a instanceof Double && b instanceof Byte) {
            return F.cast(a.doubleValue() % b.byteValue());
        }
        if (a instanceof Double && b instanceof Float) {
            return F.cast(a.doubleValue() % b.floatValue());
        }
        if (a instanceof Double && b instanceof Double) {
            return F.cast(a.doubleValue() % b.doubleValue());
        }

        //Float
        if (a instanceof Float && b instanceof Integer) {
            return F.cast(a.floatValue() % b.intValue());
        }
        if (a instanceof Float && b instanceof Long) {
            return F.cast(a.floatValue() % b.longValue());
        }
        if (a instanceof Float && b instanceof Short) {
            return F.cast(a.floatValue() % b.shortValue());
        }
        if (a instanceof Float && b instanceof Byte) {
            return F.cast(a.floatValue() % b.byteValue());
        }
        if (a instanceof Float && b instanceof Float) {
            return F.cast(a.floatValue() % b.floatValue());
        }
        if (a instanceof Float && b instanceof Double) {
            return F.cast(a.floatValue() % b.doubleValue());
        }

        //Short
        if (a instanceof Short && b instanceof Integer) {
            return F.cast(a.shortValue() % b.intValue());
        }
        if (a instanceof Short && b instanceof Long) {
            return F.cast(a.shortValue() % b.longValue());
        }
        if (a instanceof Short && b instanceof Short) {
            return F.cast(a.shortValue() % b.shortValue());
        }
        if (a instanceof Short && b instanceof Byte) {
            return F.cast(a.shortValue() % b.byteValue());
        }
        if (a instanceof Short && b instanceof Float) {
            return F.cast(a.shortValue() % b.floatValue());
        }
        if (a instanceof Short && b instanceof Double) {
            return F.cast(a.shortValue() % b.doubleValue());
        }

        //Byte
        if (a instanceof Byte && b instanceof Integer) {
            return F.cast(a.byteValue() % b.intValue());
        }
        if (a instanceof Byte && b instanceof Long) {
            return F.cast(a.byteValue() % b.longValue());
        }
        if (a instanceof Byte && b instanceof Short) {
            return F.cast(a.byteValue() % b.shortValue());
        }
        if (a instanceof Byte && b instanceof Byte) {
            return F.cast(a.byteValue() % b.byteValue());
        }
        if (a instanceof Byte && b instanceof Float) {
            return F.cast(a.byteValue() % b.floatValue());
        }
        if (a instanceof Byte && b instanceof Double) {
            return F.cast(a.byteValue() % b.doubleValue());
        }

        throw new UnsupportedOperationException("Not supported number format???");

    }
}
