package lt.lb.commons.reflect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.F;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.MethodCallSignature;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.iteration.For;
import lt.lb.commons.reflect.unified.IObjectMethod;
import lt.lb.commons.reflect.unified.ReflMethods;
import lt.lb.uncheckedutils.SafeOpt;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 *
 * JDK independent access to Unsafe class using reflection.
 *
 * @author laim0nas100
 */
public class UnsafeProvider {

    public static final String NEW_UNSAFE_CLASS = "jdk.internal.misc.Unsafe";
    public static final String OLD_UNSAFE_CLASS = "sun.misc.Unsafe";

    public static ReflUnsafe getReflUnsafe() {
        return THE_UNSAFE.get();
    }

    public static Object getUnsafe() {
        return getReflUnsafe().unsafe;
    }

    private static final SafeOpt<ReflUnsafe> THE_UNSAFE = SafeOpt.ofLazy(0)
            .flatMap(s -> {
                SafeOpt<ReflUnsafe> discoverNew = discoverNew();
                if (discoverNew.isPresent()) {
                    return discoverNew;
                }
                return discoverOld();
            });

    private static SafeOpt<ReflUnsafe> discoverNew() {
        return SafeOpt.of(NEW_UNSAFE_CLASS).map(Class::forName).map(unsafeCls -> {
            return new ReflUnsafe(unsafeCls, unsafeCls.getMethod("getUnsafe").invoke(null));
        });
    }

    private static SafeOpt<ReflUnsafe> discoverOld() {
        return SafeOpt.of(OLD_UNSAFE_CLASS).map(Class::forName).map(unsafeCls -> {
            Object theUnsafe = Refl.fieldAccessableGet(unsafeCls.getDeclaredField("theUnsafe"), null);
            return new ReflUnsafe(unsafeCls, theUnsafe);
        });
    }

    public static class ReflUnsafe extends ReflUnsafeGenerated {

        public final Class unsafeClass;
        public final Object unsafe;

        protected final Map<MethodCallSignature, IObjectMethod> unsafeMethods;

        public ReflUnsafe(Class unsafeClass, Object unsafe) {
            this.unsafeClass = Objects.requireNonNull(unsafeClass);
            this.unsafe = Objects.requireNonNull(unsafe);
            Map<MethodCallSignature, IObjectMethod> map = new HashMap<>();
            ReflMethods.getLocalMethods(unsafeClass)
                    .forEach(method -> {
                        IObjectMethod m = F.cast(method);
                        if (ReflMethods.isBaseObjectMethod(m)) {
                            return;
                        }
                        if (m.isPublic()) {
                            MethodCallSignature methodCallSignature = new MethodCallSignature(m.getName(), (Object[]) m.getParameterTypes());
                            map.put(methodCallSignature, m);
                        }

                    });
            unsafeMethods = Collections.unmodifiableMap(map);
        }

        @Override
        public Object allocateInstance(Class arg0) {
            return methodCall(new MethodCallSignature("allocateInstance", Class.class), arg0);
        }

        @Override
        protected Object methodCall(MethodCallSignature signature, Object... params) {
            IObjectMethod method = unsafeMethods.get(signature);
            if (method == null) {
                throw new IllegalAccessError(unsafeClass.getName() + " does not have a method: " + signature);
            }
            return method.safeInvoke(unsafe, params).throwAnyOrNull();
        }

    }

    public static void _printMeta() {

        Map<Class, String> primitives = ImmutableCollections.mapOf(
                Boolean.TYPE, "Boolean.TYPE",
                Character.TYPE, "Character.TYPE",
                Byte.TYPE, "Byte.TYPE",
                Short.TYPE, "Short.TYPE",
                Integer.TYPE, "Integer.TYPE",
                Long.TYPE, "Long.TYPE",
                Float.TYPE, "Float.TYPE",
                Double.TYPE, "Double.TYPE"
        );

        For.entries().iterate(getReflUnsafe().unsafeMethods, (signature, method) -> {
            LineStringBuilder sb = new LineStringBuilder();
            sb.append("public ");
            Class returnType = method.getReturnType();
            boolean noReturn = false;
            if (returnType.equals(void.class)) {
                sb.append("void ");
                noReturn = true;
            } else {
                sb.append(returnType.getSimpleName()).append(" ");
            }

            String methodName = method.getName();
            sb.append(methodName);
            int parameterCount = method.getParameterCount();
            sb.append("(");
            LineStringBuilder callParams = new LineStringBuilder();
            LineStringBuilder paramTypes = new LineStringBuilder();
            if (parameterCount > 0) {
                LineStringBuilder params = new LineStringBuilder();
                Class[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterCount; i++) {
                    if (i > 0) {
                        params.append(", ");
                        callParams.append(", ");
                        paramTypes.append(", ");
                    }

                    Class parameterType = parameterTypes[i];
                    String paramTypeName;
                    String argTypeName;
                    if (parameterType.isPrimitive()) {
                        paramTypeName = primitives.get(parameterType);
                        argTypeName = parameterType.getSimpleName();
                    } else {

                        if (parameterType.isArray()) {
                            if (parameterType.getComponentType().isPrimitive()) {//primitive array
                                argTypeName = parameterType.getSimpleName();
                                paramTypeName = argTypeName + ".class";
                            } else {
                                Class componentType = parameterType.getComponentType();
                                argTypeName = componentType.getName() + "[]";
                                if (StringUtils.countMatches(argTypeName, '.') == 2 && Strings.CS.startsWith(argTypeName, "java.lang.")) {
                                    argTypeName = Strings.CS.removeStart(argTypeName, "java.lang.");
                                }
                                paramTypeName = argTypeName + ".class";
                            }
                        } else {
                            argTypeName = parameterType.getName();
                            if (StringUtils.countMatches(argTypeName, '.') == 2 && Strings.CS.startsWith(argTypeName, "java.lang.")) {
                                argTypeName = Strings.CS.removeStart(argTypeName, "java.lang.");
                            }
                            paramTypeName = argTypeName + ".class";
                        }
                    }

                    params.append(argTypeName).append(" arg").append(i);
                    callParams.append("arg").append(i);
                    paramTypes.append(paramTypeName);
                }
                sb.append(params.toString());
            }
            sb.appendLine(") {");
            sb.append("\t");
            if (!noReturn) {
                sb.append("return ");
                if (!returnType.equals(Object.class)) {//add a cast
                    sb.append("(").append(returnType.getSimpleName()).append(") ");
                }
            }
            sb.append("methodCall(new MethodCallSignature(").append('"').append(methodName).append('"');
            if (paramTypes.length() > 0) {
                sb.append(", ");
                sb.append(paramTypes);
            }
            sb.append(")");
            if (callParams.length() > 0) {
                sb.append(", ");
                sb.append(callParams);
            }
            sb.appendLine(");");
            sb.appendLine("}");
            System.out.println(sb);
        });
    }

    private static abstract class ReflUnsafeGenerated {

        protected abstract Object methodCall(MethodCallSignature signature, Object... params);

        //the code below is generated
        public double getAndAddDoubleRelease(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndAddDoubleRelease", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public void putInt(Object arg0, long arg1, int arg2) {
            methodCall(new MethodCallSignature("putInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public void fullFence() {
            methodCall(new MethodCallSignature("fullFence"));
        }

        public long arrayBaseOffset(Class arg0) {
            return (long) methodCall(new MethodCallSignature("arrayBaseOffset", Class.class), arg0);
        }

        public Object getReferenceOpaque(Object arg0, long arg1) {
            return methodCall(new MethodCallSignature("getReferenceOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public int compareAndExchangeIntRelease(Object arg0, long arg1, int arg2, int arg3) {
            return (int) methodCall(new MethodCallSignature("compareAndExchangeIntRelease", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public int getAndBitwiseOrIntRelease(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseOrIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public byte compareAndExchangeByte(Object arg0, long arg1, byte arg2, byte arg3) {
            return (byte) methodCall(new MethodCallSignature("compareAndExchangeByte", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetBooleanPlain(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetBooleanPlain", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public Object getAndSetReferenceAcquire(Object arg0, long arg1, Object arg2) {
            return methodCall(new MethodCallSignature("getAndSetReferenceAcquire", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public Class defineClass(String arg0, byte[] arg1, int arg2, int arg3, ClassLoader arg4, java.security.ProtectionDomain arg5) {
            return (Class) methodCall(new MethodCallSignature("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ClassLoader.class, java.security.ProtectionDomain.class), arg0, arg1, arg2, arg3, arg4, arg5);
        }

        public short getAndBitwiseXorShortRelease(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseXorShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public char getCharAcquire(Object arg0, long arg1) {
            return (char) methodCall(new MethodCallSignature("getCharAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean weakCompareAndSetReference(Object arg0, long arg1, Object arg2, Object arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetReference", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public Object compareAndExchangeReferenceAcquire(Object arg0, long arg1, Object arg2, Object arg3) {
            return methodCall(new MethodCallSignature("compareAndExchangeReferenceAcquire", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public double getAndSetDoubleAcquire(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndSetDoubleAcquire", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public double getDouble(Object arg0, long arg1) {
            return (double) methodCall(new MethodCallSignature("getDouble", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean weakCompareAndSetReferencePlain(Object arg0, long arg1, Object arg2, Object arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetReferencePlain", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public boolean compareAndExchangeBooleanRelease(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndExchangeBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public long compareAndExchangeLongAcquire(Object arg0, long arg1, long arg2, long arg3) {
            return (long) methodCall(new MethodCallSignature("compareAndExchangeLongAcquire", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public char getChar(long arg0) {
            return (char) methodCall(new MethodCallSignature("getChar", Long.TYPE), arg0);
        }

        public Object getAndSetReference(Object arg0, long arg1, Object arg2) {
            return methodCall(new MethodCallSignature("getAndSetReference", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public long reallocateMemory(long arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("reallocateMemory", Long.TYPE, Long.TYPE), arg0, arg1);
        }

        public float getFloatAcquire(Object arg0, long arg1) {
            return (float) methodCall(new MethodCallSignature("getFloatAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public byte getByte(Object arg0, long arg1) {
            return (byte) methodCall(new MethodCallSignature("getByte", Object.class, Long.TYPE), arg0, arg1);
        }

        public long getLongUnaligned(Object arg0, long arg1, boolean arg2) {
            return (long) methodCall(new MethodCallSignature("getLongUnaligned", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetLongAcquire(Object arg0, long arg1, long arg2, long arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetLongAcquire", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean getAndBitwiseOrBooleanAcquire(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseOrBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public boolean compareAndSetShort(Object arg0, long arg1, short arg2, short arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetShort", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean compareAndSetBoolean(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetBoolean", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getAndBitwiseAndLong(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseAndLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public char getCharVolatile(Object arg0, long arg1) {
            return (char) methodCall(new MethodCallSignature("getCharVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public short getShortAcquire(Object arg0, long arg1) {
            return (short) methodCall(new MethodCallSignature("getShortAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean getAndBitwiseXorBooleanAcquire(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseXorBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public void putReferenceOpaque(Object arg0, long arg1, Object arg2) {
            methodCall(new MethodCallSignature("putReferenceOpaque", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public void putFloatOpaque(Object arg0, long arg1, float arg2) {
            methodCall(new MethodCallSignature("putFloatOpaque", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public void copySwapMemory(long arg0, long arg1, long arg2, long arg3) {
            methodCall(new MethodCallSignature("copySwapMemory", Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putFloatRelease(Object arg0, long arg1, float arg2) {
            methodCall(new MethodCallSignature("putFloatRelease", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public boolean getBooleanAcquire(Object arg0, long arg1) {
            return (boolean) methodCall(new MethodCallSignature("getBooleanAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public void setMemory(long arg0, long arg1, byte arg2) {
            methodCall(new MethodCallSignature("setMemory", Long.TYPE, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public byte getAndBitwiseOrByteRelease(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseOrByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public long getLong(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getLong", Object.class, Long.TYPE), arg0, arg1);
        }

        public void storeFence() {
            methodCall(new MethodCallSignature("storeFence"));
        }

        public float compareAndExchangeFloatRelease(Object arg0, long arg1, float arg2, float arg3) {
            return (float) methodCall(new MethodCallSignature("compareAndExchangeFloatRelease", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetDouble(Object arg0, long arg1, double arg2, double arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetDouble", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putFloat(Object arg0, long arg1, float arg2) {
            methodCall(new MethodCallSignature("putFloat", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public void throwException(Throwable arg0) {
            methodCall(new MethodCallSignature("throwException", Throwable.class), arg0);
        }

        public boolean weakCompareAndSetLong(Object arg0, long arg1, long arg2, long arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetLong", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetFloatPlain(Object arg0, long arg1, float arg2, float arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetFloatPlain", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putByte(Object arg0, long arg1, byte arg2) {
            methodCall(new MethodCallSignature("putByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public char getAndBitwiseXorCharRelease(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseXorCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public void putBooleanRelease(Object arg0, long arg1, boolean arg2) {
            methodCall(new MethodCallSignature("putBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetShortAcquire(Object arg0, long arg1, short arg2, short arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetShortAcquire", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getAndAddLongRelease(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndAddLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public short getAndSetShort(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndSetShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public void unpark(Object arg0) {
            methodCall(new MethodCallSignature("unpark", Object.class), arg0);
        }

        public void putCharVolatile(Object arg0, long arg1, char arg2) {
            methodCall(new MethodCallSignature("putCharVolatile", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public Object compareAndExchangeReference(Object arg0, long arg1, Object arg2, Object arg3) {
            return methodCall(new MethodCallSignature("compareAndExchangeReference", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public int getAndBitwiseOrInt(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseOrInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public int getAndBitwiseXorIntAcquire(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseXorIntAcquire", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseOrLongAcquire(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseOrLongAcquire", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public long staticFieldOffset(java.lang.reflect.Field arg0) {
            return (long) methodCall(new MethodCallSignature("staticFieldOffset", java.lang.reflect.Field.class), arg0);
        }

        public Object staticFieldBase(java.lang.reflect.Field arg0) {
            return methodCall(new MethodCallSignature("staticFieldBase", java.lang.reflect.Field.class), arg0);
        }

        public int getAndSetIntRelease(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndSetIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public char getAndAddCharRelease(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndAddCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public int getAndBitwiseAndIntRelease(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseAndIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public short getShortOpaque(Object arg0, long arg1) {
            return (short) methodCall(new MethodCallSignature("getShortOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putBooleanVolatile(Object arg0, long arg1, boolean arg2) {
            methodCall(new MethodCallSignature("putBooleanVolatile", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public Object getUncompressedObject(long arg0) {
            return methodCall(new MethodCallSignature("getUncompressedObject", Long.TYPE), arg0);
        }

        public int getIntUnaligned(Object arg0, long arg1) {
            return (int) methodCall(new MethodCallSignature("getIntUnaligned", Object.class, Long.TYPE), arg0, arg1);
        }

        public int getInt(long arg0) {
            return (int) methodCall(new MethodCallSignature("getInt", Long.TYPE), arg0);
        }

        public boolean compareAndSetChar(Object arg0, long arg1, char arg2, char arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetChar", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putIntUnaligned(Object arg0, long arg1, int arg2, boolean arg3) {
            methodCall(new MethodCallSignature("putIntUnaligned", Object.class, Long.TYPE, Integer.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public int getLoadAverage(double[] arg0, int arg1) {
            return (int) methodCall(new MethodCallSignature("getLoadAverage", double[].class, Integer.TYPE), arg0, arg1);
        }

        public char getAndBitwiseOrCharAcquire(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseOrCharAcquire", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public short getShort(Object arg0, long arg1) {
            return (short) methodCall(new MethodCallSignature("getShort", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putIntOpaque(Object arg0, long arg1, int arg2) {
            methodCall(new MethodCallSignature("putIntOpaque", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public int compareAndExchangeInt(Object arg0, long arg1, int arg2, int arg3) {
            return (int) methodCall(new MethodCallSignature("compareAndExchangeInt", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putByteOpaque(Object arg0, long arg1, byte arg2) {
            methodCall(new MethodCallSignature("putByteOpaque", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public short getAndSetShortAcquire(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndSetShortAcquire", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public long getAndSetLongRelease(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndSetLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public short getShortUnaligned(Object arg0, long arg1) {
            return (short) methodCall(new MethodCallSignature("getShortUnaligned", Object.class, Long.TYPE), arg0, arg1);
        }

        public long getAndBitwiseXorLongRelease(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseXorLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public float getAndSetFloat(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndSetFloat", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public byte getAndSetByteAcquire(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndSetByteAcquire", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public short getAndBitwiseOrShort(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseOrShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean getBooleanOpaque(Object arg0, long arg1) {
            return (boolean) methodCall(new MethodCallSignature("getBooleanOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public double getDouble(long arg0) {
            return (double) methodCall(new MethodCallSignature("getDouble", Long.TYPE), arg0);
        }

        public void putDoubleRelease(Object arg0, long arg1, double arg2) {
            methodCall(new MethodCallSignature("putDoubleRelease", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public boolean getAndSetBooleanAcquire(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndSetBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public byte getAndBitwiseOrByteAcquire(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseOrByteAcquire", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public void putShortOpaque(Object arg0, long arg1, short arg2) {
            methodCall(new MethodCallSignature("putShortOpaque", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean getBooleanVolatile(Object arg0, long arg1) {
            return (boolean) methodCall(new MethodCallSignature("getBooleanVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public short getAndBitwiseXorShort(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseXorShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public int getAndBitwiseAndInt(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseAndInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public byte getAndBitwiseAndByte(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseAndByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseOrLong(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseOrLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public short getShort(long arg0) {
            return (short) methodCall(new MethodCallSignature("getShort", Long.TYPE), arg0);
        }

        public void freeMemory(long arg0) {
            methodCall(new MethodCallSignature("freeMemory", Long.TYPE), arg0);
        }

        public boolean weakCompareAndSetBoolean(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetBoolean", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean isBigEndian() {
            return (boolean) methodCall(new MethodCallSignature("isBigEndian"));
        }

        public char compareAndExchangeCharRelease(Object arg0, long arg1, char arg2, char arg3) {
            return (char) methodCall(new MethodCallSignature("compareAndExchangeCharRelease", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public char getAndBitwiseAndCharAcquire(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseAndCharAcquire", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public long compareAndExchangeLong(Object arg0, long arg1, long arg2, long arg3) {
            return (long) methodCall(new MethodCallSignature("compareAndExchangeLong", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getLong(long arg0) {
            return (long) methodCall(new MethodCallSignature("getLong", Long.TYPE), arg0);
        }

        public void putReferenceVolatile(Object arg0, long arg1, Object arg2) {
            methodCall(new MethodCallSignature("putReferenceVolatile", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public byte getByteOpaque(Object arg0, long arg1) {
            return (byte) methodCall(new MethodCallSignature("getByteOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public char getCharUnaligned(Object arg0, long arg1) {
            return (char) methodCall(new MethodCallSignature("getCharUnaligned", Object.class, Long.TYPE), arg0, arg1);
        }

        public Object allocateUninitializedArray(Class arg0, int arg1) {
            return methodCall(new MethodCallSignature("allocateUninitializedArray", Class.class, Integer.TYPE), arg0, arg1);
        }

        public float getFloat(Object arg0, long arg1) {
            return (float) methodCall(new MethodCallSignature("getFloat", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putDouble(long arg0, double arg1) {
            methodCall(new MethodCallSignature("putDouble", Long.TYPE, Double.TYPE), arg0, arg1);
        }

        public void copySwapMemory(Object arg0, long arg1, Object arg2, long arg3, long arg4, long arg5) {
            methodCall(new MethodCallSignature("copySwapMemory", Object.class, Long.TYPE, Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3, arg4, arg5);
        }

        public boolean weakCompareAndSetChar(Object arg0, long arg1, char arg2, char arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetChar", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putShort(long arg0, short arg1) {
            methodCall(new MethodCallSignature("putShort", Long.TYPE, Short.TYPE), arg0, arg1);
        }

        public boolean compareAndExchangeBooleanAcquire(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndExchangeBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public double compareAndExchangeDoubleAcquire(Object arg0, long arg1, double arg2, double arg3) {
            return (double) methodCall(new MethodCallSignature("compareAndExchangeDoubleAcquire", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetCharRelease(Object arg0, long arg1, char arg2, char arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetCharRelease", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndBitwiseAndByteAcquire(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseAndByteAcquire", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public double getAndSetDouble(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndSetDouble", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public int addressSize() {
            return (int) methodCall(new MethodCallSignature("addressSize"));
        }

        public char getAndSetChar(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndSetChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetByte(Object arg0, long arg1, byte arg2, byte arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetByte", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean getAndSetBooleanRelease(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndSetBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public byte getAndSetByteRelease(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndSetByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetDoublePlain(Object arg0, long arg1, double arg2, double arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetDoublePlain", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putBooleanOpaque(Object arg0, long arg1, boolean arg2) {
            methodCall(new MethodCallSignature("putBooleanOpaque", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public char getCharUnaligned(Object arg0, long arg1, boolean arg2) {
            return (char) methodCall(new MethodCallSignature("getCharUnaligned", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public short compareAndExchangeShortRelease(Object arg0, long arg1, short arg2, short arg3) {
            return (short) methodCall(new MethodCallSignature("compareAndExchangeShortRelease", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public short getAndSetShortRelease(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndSetShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public void loadLoadFence() {
            methodCall(new MethodCallSignature("loadLoadFence"));
        }

        public void loadFence() {
            methodCall(new MethodCallSignature("loadFence"));
        }

        public void putLongVolatile(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putLongVolatile", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public void storeStoreFence() {
            methodCall(new MethodCallSignature("storeStoreFence"));
        }

        public int getAndBitwiseOrIntAcquire(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseOrIntAcquire", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public Object getReferenceVolatile(Object arg0, long arg1) {
            return methodCall(new MethodCallSignature("getReferenceVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean weakCompareAndSetFloatAcquire(Object arg0, long arg1, float arg2, float arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetFloatAcquire", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public char getAndSetCharAcquire(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndSetCharAcquire", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public short getAndBitwiseAndShort(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseAndShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public float getAndAddFloatAcquire(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndAddFloatAcquire", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public byte compareAndExchangeByteAcquire(Object arg0, long arg1, byte arg2, byte arg3) {
            return (byte) methodCall(new MethodCallSignature("compareAndExchangeByteAcquire", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public void ensureClassInitialized(Class arg0) {
            methodCall(new MethodCallSignature("ensureClassInitialized", Class.class), arg0);
        }

        public void putDoubleOpaque(Object arg0, long arg1, double arg2) {
            methodCall(new MethodCallSignature("putDoubleOpaque", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public void putLongOpaque(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putLongOpaque", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public int getAndBitwiseXorIntRelease(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseXorIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseOrLongRelease(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseOrLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public int getAndBitwiseAndIntAcquire(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseAndIntAcquire", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public long getLongAcquire(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getLongAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public long allocateMemory(long arg0) {
            return (long) methodCall(new MethodCallSignature("allocateMemory", Long.TYPE), arg0);
        }

        public void putDouble(Object arg0, long arg1, double arg2) {
            methodCall(new MethodCallSignature("putDouble", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public int getAndSetIntAcquire(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndSetIntAcquire", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public void putByteRelease(Object arg0, long arg1, byte arg2) {
            methodCall(new MethodCallSignature("putByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public boolean compareAndExchangeBoolean(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndExchangeBoolean", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getLongVolatile(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getLongVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public int getIntOpaque(Object arg0, long arg1) {
            return (int) methodCall(new MethodCallSignature("getIntOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean weakCompareAndSetByteAcquire(Object arg0, long arg1, byte arg2, byte arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetByteAcquire", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putInt(long arg0, int arg1) {
            methodCall(new MethodCallSignature("putInt", Long.TYPE, Integer.TYPE), arg0, arg1);
        }

        public long getLongUnaligned(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getLongUnaligned", Object.class, Long.TYPE), arg0, arg1);
        }

        public short getAndBitwiseOrShortRelease(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseOrShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseAndLongRelease(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseAndLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public void putChar(Object arg0, long arg1, char arg2) {
            methodCall(new MethodCallSignature("putChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public Object getReferenceAcquire(Object arg0, long arg1) {
            return methodCall(new MethodCallSignature("getReferenceAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public Object getReference(Object arg0, long arg1) {
            return methodCall(new MethodCallSignature("getReference", Object.class, Long.TYPE), arg0, arg1);
        }

        public short getAndBitwiseAndShortRelease(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseAndShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public double compareAndExchangeDouble(Object arg0, long arg1, double arg2, double arg3) {
            return (double) methodCall(new MethodCallSignature("compareAndExchangeDouble", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean getAndBitwiseOrBoolean(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseOrBoolean", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseXorLongAcquire(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseXorLongAcquire", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public long objectFieldOffset(Class arg0, String arg1) {
            return (long) methodCall(new MethodCallSignature("objectFieldOffset", Class.class, String.class), arg0, arg1);
        }

        public double compareAndExchangeDoubleRelease(Object arg0, long arg1, double arg2, double arg3) {
            return (double) methodCall(new MethodCallSignature("compareAndExchangeDoubleRelease", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public char getAndAddChar(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndAddChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public void putShort(Object arg0, long arg1, short arg2) {
            methodCall(new MethodCallSignature("putShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public char getAndBitwiseXorChar(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseXorChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public void writebackMemory(long arg0, long arg1) {
            methodCall(new MethodCallSignature("writebackMemory", Long.TYPE, Long.TYPE), arg0, arg1);
        }

        public boolean compareAndSetDouble(Object arg0, long arg1, double arg2, double arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetDouble", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public int arrayIndexScale(Class arg0) {
            return (int) methodCall(new MethodCallSignature("arrayIndexScale", Class.class), arg0);
        }

        public boolean weakCompareAndSetFloatRelease(Object arg0, long arg1, float arg2, float arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetFloatRelease", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean compareAndSetByte(Object arg0, long arg1, byte arg2, byte arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetByte", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public short getShortVolatile(Object arg0, long arg1) {
            return (short) methodCall(new MethodCallSignature("getShortVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean getBoolean(Object arg0, long arg1) {
            return (boolean) methodCall(new MethodCallSignature("getBoolean", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean compareAndSetReference(Object arg0, long arg1, Object arg2, Object arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetReference", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public byte compareAndExchangeByteRelease(Object arg0, long arg1, byte arg2, byte arg3) {
            return (byte) methodCall(new MethodCallSignature("compareAndExchangeByteRelease", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public int pageSize() {
            return (int) methodCall(new MethodCallSignature("pageSize"));
        }

        public byte getAndBitwiseXorByteAcquire(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseXorByteAcquire", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public long getAndAddLongAcquire(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndAddLongAcquire", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public void invokeCleaner(java.nio.ByteBuffer arg0) {
            methodCall(new MethodCallSignature("invokeCleaner", java.nio.ByteBuffer.class), arg0);
        }

        public double getAndSetDoubleRelease(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndSetDoubleRelease", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetFloat(Object arg0, long arg1, float arg2, float arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetFloat", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public float getAndAddFloatRelease(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndAddFloatRelease", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public void park(boolean arg0, long arg1) {
            methodCall(new MethodCallSignature("park", Boolean.TYPE, Long.TYPE), arg0, arg1);
        }

        public boolean getAndBitwiseXorBooleanRelease(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseXorBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public void putIntRelease(Object arg0, long arg1, int arg2) {
            methodCall(new MethodCallSignature("putIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public boolean unalignedAccess() {
            return (boolean) methodCall(new MethodCallSignature("unalignedAccess"));
        }

        public boolean weakCompareAndSetDoubleAcquire(Object arg0, long arg1, double arg2, double arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetDoubleAcquire", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getAndBitwiseXorLong(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseXorLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public void putCharRelease(Object arg0, long arg1, char arg2) {
            methodCall(new MethodCallSignature("putCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public double getDoubleOpaque(Object arg0, long arg1) {
            return (double) methodCall(new MethodCallSignature("getDoubleOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public Object allocateInstance(Class arg0) {
            return methodCall(new MethodCallSignature("allocateInstance", Class.class), arg0);
        }

        public byte getAndAddByteAcquire(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndAddByteAcquire", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public void putChar(long arg0, char arg1) {
            methodCall(new MethodCallSignature("putChar", Long.TYPE, Character.TYPE), arg0, arg1);
        }

        public int getInt(Object arg0, long arg1) {
            return (int) methodCall(new MethodCallSignature("getInt", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putShortRelease(Object arg0, long arg1, short arg2) {
            methodCall(new MethodCallSignature("putShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public int getIntUnaligned(Object arg0, long arg1, boolean arg2) {
            return (int) methodCall(new MethodCallSignature("getIntUnaligned", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public boolean getAndBitwiseOrBooleanRelease(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseOrBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetIntAcquire(Object arg0, long arg1, int arg2, int arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetIntAcquire", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public int getIntVolatile(Object arg0, long arg1) {
            return (int) methodCall(new MethodCallSignature("getIntVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public void copyMemory(long arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("copyMemory", Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public short compareAndExchangeShort(Object arg0, long arg1, short arg2, short arg3) {
            return (short) methodCall(new MethodCallSignature("compareAndExchangeShort", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public short getAndBitwiseAndShortAcquire(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseAndShortAcquire", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetShort(Object arg0, long arg1, short arg2, short arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetShort", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetReferenceAcquire(Object arg0, long arg1, Object arg2, Object arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetReferenceAcquire", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public void putCharUnaligned(Object arg0, long arg1, char arg2, boolean arg3) {
            methodCall(new MethodCallSignature("putCharUnaligned", Object.class, Long.TYPE, Character.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndBitwiseOrByte(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseOrByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public int compareAndExchangeIntAcquire(Object arg0, long arg1, int arg2, int arg3) {
            return (int) methodCall(new MethodCallSignature("compareAndExchangeIntAcquire", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public double getDoubleAcquire(Object arg0, long arg1) {
            return (double) methodCall(new MethodCallSignature("getDoubleAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public int getAndAddIntAcquire(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndAddIntAcquire", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public Object getAndSetReferenceRelease(Object arg0, long arg1, Object arg2) {
            return methodCall(new MethodCallSignature("getAndSetReferenceRelease", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public char getChar(Object arg0, long arg1) {
            return (char) methodCall(new MethodCallSignature("getChar", Object.class, Long.TYPE), arg0, arg1);
        }

        public int getAndSetInt(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndSetInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public short getAndBitwiseXorShortAcquire(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseXorShortAcquire", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetBooleanAcquire(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetShortPlain(Object arg0, long arg1, short arg2, short arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetShortPlain", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public int getAndBitwiseXorInt(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndBitwiseXorInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public void setMemory(Object arg0, long arg1, long arg2, byte arg3) {
            methodCall(new MethodCallSignature("setMemory", Object.class, Long.TYPE, Long.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public float getAndSetFloatRelease(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndSetFloatRelease", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetCharPlain(Object arg0, long arg1, char arg2, char arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetCharPlain", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public long objectFieldOffset(java.lang.reflect.Field arg0) {
            return (long) methodCall(new MethodCallSignature("objectFieldOffset", java.lang.reflect.Field.class), arg0);
        }

        public char getAndSetCharRelease(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndSetCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public boolean getAndBitwiseAndBooleanRelease(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseAndBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public float getFloatOpaque(Object arg0, long arg1) {
            return (float) methodCall(new MethodCallSignature("getFloatOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putShortVolatile(Object arg0, long arg1, short arg2) {
            methodCall(new MethodCallSignature("putShortVolatile", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetLongPlain(Object arg0, long arg1, long arg2, long arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetLongPlain", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public Class defineClass0(String arg0, byte[] arg1, int arg2, int arg3, ClassLoader arg4, java.security.ProtectionDomain arg5) {
            return (Class) methodCall(new MethodCallSignature("defineClass0", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ClassLoader.class, java.security.ProtectionDomain.class), arg0, arg1, arg2, arg3, arg4, arg5);
        }

        public void putCharOpaque(Object arg0, long arg1, char arg2) {
            methodCall(new MethodCallSignature("putCharOpaque", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public short getAndBitwiseOrShortAcquire(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndBitwiseOrShortAcquire", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public void putIntVolatile(Object arg0, long arg1, int arg2) {
            methodCall(new MethodCallSignature("putIntVolatile", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public void putShortUnaligned(Object arg0, long arg1, short arg2, boolean arg3) {
            methodCall(new MethodCallSignature("putShortUnaligned", Object.class, Long.TYPE, Short.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean compareAndSetInt(Object arg0, long arg1, int arg2, int arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetInt", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetInt(Object arg0, long arg1, int arg2, int arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetInt", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getByte(long arg0) {
            return (byte) methodCall(new MethodCallSignature("getByte", Long.TYPE), arg0);
        }

        public void putIntUnaligned(Object arg0, long arg1, int arg2) {
            methodCall(new MethodCallSignature("putIntUnaligned", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetByteRelease(Object arg0, long arg1, byte arg2, byte arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetByteRelease", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndSetByte(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndSetByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public float getAndAddFloat(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndAddFloat", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public long getAndBitwiseAndLongAcquire(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndBitwiseAndLongAcquire", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public float getFloat(long arg0) {
            return (float) methodCall(new MethodCallSignature("getFloat", Long.TYPE), arg0);
        }

        public boolean compareAndSetLong(Object arg0, long arg1, long arg2, long arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetLong", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public short compareAndExchangeShortAcquire(Object arg0, long arg1, short arg2, short arg3) {
            return (short) methodCall(new MethodCallSignature("compareAndExchangeShortAcquire", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public float compareAndExchangeFloatAcquire(Object arg0, long arg1, float arg2, float arg3) {
            return (float) methodCall(new MethodCallSignature("compareAndExchangeFloatAcquire", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndBitwiseAndByteRelease(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseAndByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public void putLongUnaligned(Object arg0, long arg1, long arg2, boolean arg3) {
            methodCall(new MethodCallSignature("putLongUnaligned", Object.class, Long.TYPE, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public char getAndBitwiseOrChar(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseOrChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public void putLongRelease(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putLongRelease", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public int getAndAddIntRelease(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndAddIntRelease", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public boolean shouldBeInitialized(Class arg0) {
            return (boolean) methodCall(new MethodCallSignature("shouldBeInitialized", Class.class), arg0);
        }

        public long dataCacheLineAlignDown(long arg0) {
            return (long) methodCall(new MethodCallSignature("dataCacheLineAlignDown", Long.TYPE), arg0);
        }

        public void putReferenceRelease(Object arg0, long arg1, Object arg2) {
            methodCall(new MethodCallSignature("putReferenceRelease", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public short getAndAddShort(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndAddShort", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public byte getByteAcquire(Object arg0, long arg1) {
            return (byte) methodCall(new MethodCallSignature("getByteAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public int dataCacheLineFlushSize() {
            return (int) methodCall(new MethodCallSignature("dataCacheLineFlushSize"));
        }

        public boolean weakCompareAndSetDoubleRelease(Object arg0, long arg1, double arg2, double arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetDoubleRelease", Object.class, Long.TYPE, Double.TYPE, Double.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putLongUnaligned(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putLongUnaligned", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public short getAndAddShortAcquire(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndAddShortAcquire", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetReferenceRelease(Object arg0, long arg1, Object arg2, Object arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetReferenceRelease", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public char getAndBitwiseXorCharAcquire(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseXorCharAcquire", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetBooleanRelease(Object arg0, long arg1, boolean arg2, boolean arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetBooleanRelease", Object.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndBitwiseXorByteRelease(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseXorByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public Object compareAndExchangeReferenceRelease(Object arg0, long arg1, Object arg2, Object arg3) {
            return methodCall(new MethodCallSignature("compareAndExchangeReferenceRelease", Object.class, Long.TYPE, Object.class, Object.class), arg0, arg1, arg2, arg3);
        }

        public long getAddress(long arg0) {
            return (long) methodCall(new MethodCallSignature("getAddress", Long.TYPE), arg0);
        }

        public void putLong(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public long compareAndExchangeLongRelease(Object arg0, long arg1, long arg2, long arg3) {
            return (long) methodCall(new MethodCallSignature("compareAndExchangeLongRelease", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getAndSetLong(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndSetLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public double getAndAddDouble(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndAddDouble", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public byte getAndBitwiseXorByte(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndBitwiseXorByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public char compareAndExchangeCharAcquire(Object arg0, long arg1, char arg2, char arg3) {
            return (char) methodCall(new MethodCallSignature("compareAndExchangeCharAcquire", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public void copyMemory(Object arg0, long arg1, Object arg2, long arg3, long arg4) {
            methodCall(new MethodCallSignature("copyMemory", Object.class, Long.TYPE, Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3, arg4);
        }

        public boolean weakCompareAndSetIntRelease(Object arg0, long arg1, int arg2, int arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetIntRelease", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetCharAcquire(Object arg0, long arg1, char arg2, char arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetCharAcquire", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public byte getAndAddByteRelease(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndAddByteRelease", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetBytePlain(Object arg0, long arg1, byte arg2, byte arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetBytePlain", Object.class, Long.TYPE, Byte.TYPE, Byte.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean weakCompareAndSetLongRelease(Object arg0, long arg1, long arg2, long arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetLongRelease", Object.class, Long.TYPE, Long.TYPE, Long.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean getAndBitwiseXorBoolean(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseXorBoolean", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public short getShortUnaligned(Object arg0, long arg1, boolean arg2) {
            return (short) methodCall(new MethodCallSignature("getShortUnaligned", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public char compareAndExchangeChar(Object arg0, long arg1, char arg2, char arg3) {
            return (char) methodCall(new MethodCallSignature("compareAndExchangeChar", Object.class, Long.TYPE, Character.TYPE, Character.TYPE), arg0, arg1, arg2, arg3);
        }

        public long getAddress(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getAddress", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean getAndSetBoolean(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndSetBoolean", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public void putDoubleVolatile(Object arg0, long arg1, double arg2) {
            methodCall(new MethodCallSignature("putDoubleVolatile", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public void putAddress(Object arg0, long arg1, long arg2) {
            methodCall(new MethodCallSignature("putAddress", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public void putAddress(long arg0, long arg1) {
            methodCall(new MethodCallSignature("putAddress", Long.TYPE, Long.TYPE), arg0, arg1);
        }

        public void putFloat(long arg0, float arg1) {
            methodCall(new MethodCallSignature("putFloat", Long.TYPE, Float.TYPE), arg0, arg1);
        }

        public short getAndAddShortRelease(Object arg0, long arg1, short arg2) {
            return (short) methodCall(new MethodCallSignature("getAndAddShortRelease", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public void putReference(Object arg0, long arg1, Object arg2) {
            methodCall(new MethodCallSignature("putReference", Object.class, Long.TYPE, Object.class), arg0, arg1, arg2);
        }

        public byte getAndAddByte(Object arg0, long arg1, byte arg2) {
            return (byte) methodCall(new MethodCallSignature("getAndAddByte", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public long getLongOpaque(Object arg0, long arg1) {
            return (long) methodCall(new MethodCallSignature("getLongOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean compareAndSetFloat(Object arg0, long arg1, float arg2, float arg3) {
            return (boolean) methodCall(new MethodCallSignature("compareAndSetFloat", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putByte(long arg0, byte arg1) {
            methodCall(new MethodCallSignature("putByte", Long.TYPE, Byte.TYPE), arg0, arg1);
        }

        public void putByteVolatile(Object arg0, long arg1, byte arg2) {
            methodCall(new MethodCallSignature("putByteVolatile", Object.class, Long.TYPE, Byte.TYPE), arg0, arg1, arg2);
        }

        public int getIntAcquire(Object arg0, long arg1) {
            return (int) methodCall(new MethodCallSignature("getIntAcquire", Object.class, Long.TYPE), arg0, arg1);
        }

        public char getAndAddCharAcquire(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndAddCharAcquire", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public boolean getAndBitwiseAndBoolean(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseAndBoolean", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public float compareAndExchangeFloat(Object arg0, long arg1, float arg2, float arg3) {
            return (float) methodCall(new MethodCallSignature("compareAndExchangeFloat", Object.class, Long.TYPE, Float.TYPE, Float.TYPE), arg0, arg1, arg2, arg3);
        }

        public boolean getAndBitwiseAndBooleanAcquire(Object arg0, long arg1, boolean arg2) {
            return (boolean) methodCall(new MethodCallSignature("getAndBitwiseAndBooleanAcquire", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public char getAndBitwiseAndCharRelease(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseAndCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public char getAndBitwiseAndChar(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseAndChar", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public float getAndSetFloatAcquire(Object arg0, long arg1, float arg2) {
            return (float) methodCall(new MethodCallSignature("getAndSetFloatAcquire", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public byte getByteVolatile(Object arg0, long arg1) {
            return (byte) methodCall(new MethodCallSignature("getByteVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public float getFloatVolatile(Object arg0, long arg1) {
            return (float) methodCall(new MethodCallSignature("getFloatVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public void putFloatVolatile(Object arg0, long arg1, float arg2) {
            methodCall(new MethodCallSignature("putFloatVolatile", Object.class, Long.TYPE, Float.TYPE), arg0, arg1, arg2);
        }

        public boolean weakCompareAndSetShortRelease(Object arg0, long arg1, short arg2, short arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetShortRelease", Object.class, Long.TYPE, Short.TYPE, Short.TYPE), arg0, arg1, arg2, arg3);
        }

        public void putLong(long arg0, long arg1) {
            methodCall(new MethodCallSignature("putLong", Long.TYPE, Long.TYPE), arg0, arg1);
        }

        public int getAndAddInt(Object arg0, long arg1, int arg2) {
            return (int) methodCall(new MethodCallSignature("getAndAddInt", Object.class, Long.TYPE, Integer.TYPE), arg0, arg1, arg2);
        }

        public void putShortUnaligned(Object arg0, long arg1, short arg2) {
            methodCall(new MethodCallSignature("putShortUnaligned", Object.class, Long.TYPE, Short.TYPE), arg0, arg1, arg2);
        }

        public void putCharUnaligned(Object arg0, long arg1, char arg2) {
            methodCall(new MethodCallSignature("putCharUnaligned", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public char getAndBitwiseOrCharRelease(Object arg0, long arg1, char arg2) {
            return (char) methodCall(new MethodCallSignature("getAndBitwiseOrCharRelease", Object.class, Long.TYPE, Character.TYPE), arg0, arg1, arg2);
        }

        public void putBoolean(Object arg0, long arg1, boolean arg2) {
            methodCall(new MethodCallSignature("putBoolean", Object.class, Long.TYPE, Boolean.TYPE), arg0, arg1, arg2);
        }

        public double getDoubleVolatile(Object arg0, long arg1) {
            return (double) methodCall(new MethodCallSignature("getDoubleVolatile", Object.class, Long.TYPE), arg0, arg1);
        }

        public boolean weakCompareAndSetIntPlain(Object arg0, long arg1, int arg2, int arg3) {
            return (boolean) methodCall(new MethodCallSignature("weakCompareAndSetIntPlain", Object.class, Long.TYPE, Integer.TYPE, Integer.TYPE), arg0, arg1, arg2, arg3);
        }

        public double getAndAddDoubleAcquire(Object arg0, long arg1, double arg2) {
            return (double) methodCall(new MethodCallSignature("getAndAddDoubleAcquire", Object.class, Long.TYPE, Double.TYPE), arg0, arg1, arg2);
        }

        public long getAndAddLong(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndAddLong", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

        public char getCharOpaque(Object arg0, long arg1) {
            return (char) methodCall(new MethodCallSignature("getCharOpaque", Object.class, Long.TYPE), arg0, arg1);
        }

        public long getAndSetLongAcquire(Object arg0, long arg1, long arg2) {
            return (long) methodCall(new MethodCallSignature("getAndSetLongAcquire", Object.class, Long.TYPE, Long.TYPE), arg0, arg1, arg2);
        }

    }
}
