/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;

/**
 *
 * @author Lemmin
 */
public class FieldFac {

    public static final Class[] NUMBER_TYPES = {Number.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};
    public static final Class[] DATE_TYPES = {LocalDate.class, LocalTime.class, LocalDateTime.class};
    public static final Class[] OTHER_IMMUTABLE_TYPES = {Boolean.class, String.class, Character.class, Object.class};
    public static final Class[] IMMUTABLE_TYPES = ArrayOp.merge(OTHER_IMMUTABLE_TYPES, NUMBER_TYPES, DATE_TYPES);
    public static final Predicate<Class> isImmutable = (Class cls) -> {
        if (cls.isPrimitive()) {
            return true;
        }
        return (ArrayOp.count(Predicate.isEqual(cls), IMMUTABLE_TYPES) > 0);
    };

    public static FieldResolver makeImmutableFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> f.set(parentObject, f.get(sourceObject));
    }

    public static FieldResolver makeImmutableArrayFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {

            Class compType = f.getType().getComponentType();
            Log.print("Array of Immutable ", compType.getName());
            Object sourceArray = f.get(sourceObject);
            boolean needRegistration = false;
            if (refCounter.contains(sourceArray)) {
                Log.print("Found repeating immutable array reference");
                f.set(parentObject, refCounter.get(sourceArray));
            } else {
                int length = Array.getLength(sourceArray);
                Object array = null;
                if (compType.isPrimitive()) {
                    array = ArrayOp.makePrimitiveArray(length, compType);
                } else {
                    array = ArrayOp.makeArray(length, compType);
                }

                System.arraycopy(sourceArray, 0, array, 0, length);
                f.set(parentObject, array);
                refCounter.registerIfAbsent(sourceArray, array);
            }

        };
    }

    public static FieldResolver makeEnumFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {

            Enum en = (Enum) f.get(sourceObject);
            String name = en.name();
            Log.print("Found enum " + f.toString() + " with name:" + name);
            f.set(parentObject, Enum.valueOf((Class<Enum>) f.getType(), name));
        };
    }

    public static <T> T createNewInstance(Class<T> cls) throws Exception {
        boolean isAbstract = Modifier.isAbstract(cls.getModifiers());
        if (isAbstract) {
            throw new IllegalArgumentException("Cant initialize abstract class " + cls.getName());
        }
        if (cls.isInterface()) {
            throw new IllegalArgumentException("Cant initialize interface " + cls.getName() + " pass implementation class");
        }
        if (cls.isEnum()) {
            throw new IllegalArgumentException("Cant initialize enum " + cls.getName());
        }
        Object newInstance = null;

        try {
            newInstance = cls.newInstance();
            Log.print("Easy instantiation " + cls.getCanonicalName());
            return (T) newInstance;
        } catch (InstantiationException | IllegalAccessException e) {
        }

        Constructor<?>[] declaredConstructors = cls.getDeclaredConstructors();
        // sort by lower parameter constructor first
        Arrays.sort(declaredConstructors, (o1, o2) -> o1.getParameterCount() - o2.getParameterCount());

        for (int i = 0; i < declaredConstructors.length; i++) {
            Constructor<?> cons = declaredConstructors[i];
            cons.setAccessible(true);

            int argCount = cons.getParameterCount();
            Object[] makeArray = ArrayOp.makeArray(argCount, Object.class);
            try {
                Log.print("Try with " + cons);
                Log.print(makeArray);
                newInstance = cons.newInstance(makeArray);

                if (newInstance != null) {
                    Log.print("We good");
                    // we good
                    return (T) newInstance;
                }
            } catch (InstantiationException e) {
//                e.printStackTrace(System.err);
            }
        }
        throw new IllegalStateException("Failed to instantiate class " + cls.getName());
    }

    public static boolean isInitializable(Class cls) {
        if (cls.isEnum() || cls.isInterface() || cls.isPrimitive() || cls.isArray()) {
            return false;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        return true;
    }

    public static FieldResolver recursiveResolver(final Class startingClass, final Map<Class, ExplicitClone> predefinedClone, final Map<Class, FieldResolver> cachedResolvers) {
        Map<String, FieldResolver> resolverMap = new HashMap<>();
        Class currentClass = startingClass;
        while (currentClass != null) {
            FieldHolder holder = new FieldHolder(currentClass);
            for (Field f : holder.getFields().values()) {
                f.setAccessible(true);
                final Class fieldType = f.getType();
                final boolean isInitilizable = FieldFac.isInitializable(fieldType);

                boolean isSingular = !fieldType.isArray();

                FieldResolver fr = null;
                //cache resolvers, with [class + name]

                if (isSingular) {
                    if (fieldType.isEnum()) {
                        fr = makeEnumFieldResolver(f);
                    } else if (isImmutable.test(fieldType)) {
                        fr = makeImmutableFieldResolver(f);
                    } else {

                        fr = new FieldResolver() {
                            @Override
                            public void cloneField(Object sourceObject, Object parentObject, ReferenceCounter refCounter) throws Exception {

                                //maybe clone object?
                                //TODO if repeated reference, don't clone, just assign
                                // look up predefined resolvers
                                if (predefinedClone.containsKey(fieldType)) {
                                    Object value = f.get(sourceObject);
                                    Object clonedValue = predefinedClone.get(fieldType).clone(value);
                                    f.set(parentObject, clonedValue);
                                    return;
                                }

                                Object sourceInstance = f.get(sourceObject);
                                if (refCounter.contains(sourceInstance)) {
                                    Log.print("Found repeating reference");
                                    f.set(parentObject, refCounter.get(sourceInstance));

                                } else {
                                    if (sourceInstance == null) {
                                        Log.print("Found null", f);
                                        f.set(parentObject, null);
                                    } else {

                                        Class realClass = fieldType;

                                        if (!isInitilizable) {
                                            realClass = sourceObject.getClass();
                                        }
                                        Object newInstance = createNewInstance(realClass);
                                        refCounter.registerIfAbsent(sourceInstance, newInstance);

                                        FieldResolver rr = recursiveResolver(fieldType, predefinedClone, cachedResolvers);

                                        //recursive downward clone
                                        rr.cloneField(sourceInstance, newInstance, refCounter);

                                        f.set(parentObject, newInstance);
                                    }

                                }

                            }
                        };

                    }
                } else { // is array
                    final Class compType = fieldType.getComponentType();
                    Log.print("Array of " + compType.getName());
                    if (isImmutable.test(compType)) {
                        fr = makeImmutableArrayFieldResolver(f);
                    } else {
                        fr = new FieldResolver() {
                            @Override
                            public void cloneField(Object source, Object parentObject, ReferenceCounter refCounter) throws Exception {
                                Object[] sourceArray = (Object[]) f.get(source);

                                if (refCounter.contains(sourceArray)) {
                                    Log.print("Found repeating " + compType.getName() + " array reference");
                                    f.set(parentObject, refCounter.get(sourceArray));
                                } else {

                                    int length = Array.getLength(sourceArray);
                                    Object[] newArray = ArrayOp.makeArray(length, compType);

                                    final boolean isInitilizable = !(Modifier.isAbstract(compType.getModifiers()) || compType.isInterface());

                                    for (int i = 0; i < length; i++) {
                                        Object sourceInstance = sourceArray[i];

                                        Object newInstance = null;

                                        if (sourceInstance != null) {
                                            FieldResolver rr = null;

                                            if (isInitilizable) {
                                                rr = recursiveResolver(compType, predefinedClone, cachedResolvers);
                                                newInstance = createNewInstance(compType);
                                            } else { // get real component type
                                                Class realClass = sourceInstance.getClass();
                                                newInstance = createNewInstance(realClass);
                                                rr = recursiveResolver(realClass, predefinedClone, cachedResolvers);
                                            }

                                            rr.cloneField(sourceInstance, newInstance, refCounter);
                                            Array.set(newArray, i, sourceArray);
                                        }

                                    }

                                    f.set(parentObject, newArray);
                                    refCounter.registerIfAbsent(sourceArray, newArray);
                                }
                            }
                        };
                    }

                }
                resolverMap.putIfAbsent(f.getName(), fr); // put with shadowing

            }

            currentClass = currentClass.getSuperclass();
        }
        return new FieldResolver() { // combine field from map
            @Override
            public void cloneField(Object source, Object parentObject, ReferenceCounter refCounter) throws Exception {
                for (FieldResolver resolver : resolverMap.values()) {
                    resolver.cloneField(source, parentObject, refCounter);
                }
            }
        };
    }

    private Map<Class, ExplicitClone> predefinedClone = new ConcurrentHashMap<>();

    public FieldFac() {
//        Date.class //        predefinedClone.put(Date.class, (ExplicitClone<Date>) (Date value) -> {
        //                        return new Date(value.getTime());
        //                    });
    }

    public <T> T reflectionClone(T source) throws Exception {
        Class<T> cls = (Class<T>) source.getClass();
        T newInstance = createNewInstance(cls);

        FieldResolver recursiveResolver = recursiveResolver(cls, predefinedClone, new HashMap<>());
        recursiveResolver.cloneField(source, newInstance, new ReferenceCounter());
        return newInstance;

    }
}
