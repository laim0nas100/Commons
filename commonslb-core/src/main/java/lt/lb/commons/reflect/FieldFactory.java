/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Log;

/**
 *
 * @author Lemmin
 */
public abstract class FieldFactory {

    public static final Class[] NUMBER_TYPES = {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigDecimal.class, BigInteger.class};
    public static final Class[] DATE_TYPES = {LocalDate.class, LocalTime.class, LocalDateTime.class};
    public static final Class[] OTHER_IMMUTABLE_TYPES = {Boolean.class, String.class, Character.class, UUID.class, Pattern.class};
    public static final Class[] JDK_IMMUTABLE_TYPES = ArrayOp.merge(OTHER_IMMUTABLE_TYPES, NUMBER_TYPES, DATE_TYPES);
    public static final Predicate<Class> isJDKImmutable = (Class cls) -> {
        if (cls.isPrimitive()) {
            return true;
        }
        return (ArrayOp.count(Predicate.isEqual(cls), JDK_IMMUTABLE_TYPES) > 0);
    };

    public static IFieldResolver makeImmutableFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> f.set(parentObject, f.get(sourceObject));
    }

    public static IFieldResolver makeImmutableArrayFieldResolver(Field f) {
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

    public static IFieldResolver makeEnumFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {

//            Enum en = (Enum) f.get(sourceObject);
//            String name = en.name();
//            Log.print("Found enum " + f.toString() + " with name:" + name);
//            f.set(parentObject, Enum.valueOf((Class<Enum>) f.getType(), name));
            f.set(parentObject, cloneEnum(f.get(sourceObject)));
        };
    }

    public static Enum cloneEnum(Object ob) {
        Enum en = (Enum) ob;
        Class<Enum> cls = (Class<Enum>) ob.getClass();
        return Enum.valueOf(cls, en.name());
    }

    public <T> T createNewInstance(Class<T> cls) {

        //check class constructors
        if (this.predefinedClassConstruct.containsKey(cls)) {
            T val = (T) this.predefinedClassConstruct.get(cls).construct();
            return val;
        }

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
            Log.print("Easy instantiation " + cls.getName());
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
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//                e.printStackTrace(System.err);
            }
        }
        throw new IllegalStateException("Failed to instantiate class " + cls.getName());
    }

    public boolean isInitializable(Class cls) {
        if (cls.isEnum() || cls.isInterface() || cls.isPrimitive() || cls.isArray()) {
            return false;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        return !this.isImmutable(cls);
    }

    private IFieldResolver recursiveResolver(final Class startingClass) {
        if(!this.useCache){
            return this.recursiveResolverCached(startingClass);
        }
        if (this.cachedFieldResolvers.containsKey(startingClass)) {
            return this.cachedFieldResolvers.get(startingClass);
        } else {
            IFieldResolver resolver = recursiveResolverCached(startingClass);
            this.cachedFieldResolvers.put(startingClass, resolver);
            return resolver;
        }
    }

    private IFieldResolver recursiveResolverCached(final Class startingClass) {
        Map<String, IFieldResolver> resolverMap = new HashMap<>();
        Class currentClass = startingClass;
        while (currentClass != null) {
            FieldHolder holder = new FieldHolder(currentClass);
            for (Field f : holder.getFields().values()) {
                f.setAccessible(true);
                final Class fieldType = f.getType();
                final boolean isInitilizable = isInitializable(fieldType);

                boolean isSingular = !fieldType.isArray();

                IFieldResolver fr = null;
                //cache resolvers, with [class + name]

                if (isSingular) {
                    if (fieldType.isEnum()) {
                        Log.print("is enum", f);
                        fr = makeEnumFieldResolver(f);
                    } else if (isImmutable(fieldType)) {
                        Log.print("is immutable", f);
                        fr = makeImmutableFieldResolver(f);
                    } else {

                        Log.print("is composite", f);
                        fr = new IFieldResolver() {
                            @Override
                            public void cloneField(Object sourceObject, Object parentObject, ReferenceCounter refCounter) throws Exception {

                                //maybe clone object?
                                //TODO if repeated reference, don't clone, just assign
                                // look up predefined resolvers
                                Object sourceInstance = f.get(sourceObject);

                                if (refCounter.contains(sourceInstance)) {
                                    Log.print("Found repeating reference");
                                    f.set(parentObject, refCounter.get(sourceInstance));

                                } else {
                                    if (predefinedClone.containsKey(fieldType)) {
                                        Log.print("Found predefined clone of " + fieldType.getName());
                                        Object clonedValue = predefinedClone.get(fieldType).clone(sourceInstance);
                                        f.set(parentObject, clonedValue);
                                        Log.print("After predefined clone set");
                                        return;
                                    }
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

                                        IFieldResolver rr = recursiveResolver(fieldType);

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
                    if (isImmutable(compType)) {
                        fr = makeImmutableArrayFieldResolver(f);
                    } else {
                        fr = new IFieldResolver() {
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
                                            IFieldResolver rr = null;

                                            if (compType.isEnum()) {
                                                newInstance = cloneEnum(sourceInstance);
                                            } else {
                                                if (isInitilizable) {
                                                    rr = recursiveResolver(compType);
                                                    newInstance = createNewInstance(compType);
                                                } else { // get real component type
                                                    Class realClass = sourceInstance.getClass();
                                                    newInstance = createNewInstance(realClass);
                                                    rr = recursiveResolver(realClass);

                                                }

                                                rr.cloneField(sourceInstance, newInstance, refCounter);
                                            }
                                            
                                            Array.set(newArray, i, newInstance);
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
        return new IFieldResolver() { // combine field from map
            @Override
            public void cloneField(Object source, Object parentObject, ReferenceCounter refCounter) throws Exception {
                for (IFieldResolver resolver : resolverMap.values()) {
                    resolver.cloneField(source, parentObject, refCounter);
                }
            }
        };
    }

    private Map<Class, IExplicitClone> predefinedClone = new ConcurrentHashMap<>();
    private Map<Class, IClassConstructor> predefinedClassConstruct = new ConcurrentHashMap<>();
    private Map<Class, IFieldResolver> cachedFieldResolvers = new ConcurrentHashMap<>();
    private Set<Class> immutableTypes = ConcurrentHashMap.newKeySet();

    
    public boolean useCache = true;
    public FieldFactory() {
    }

    public <T> T reflectionClone(T source) throws Exception {
        Class<T> cls = (Class<T>) source.getClass();
        T newInstance = createNewInstance(cls);

        IFieldResolver recursiveResolver = recursiveResolver(cls);
        recursiveResolver.cloneField(source, newInstance, newReferenceCounter());
        return newInstance;

    }

    public <E> FieldFactory addExplicitClone(Class<E> cls, IExplicitClone<E> cloneFunc) {
        this.predefinedClone.put(cls, cloneFunc);
        return this;
    }

    public <E> FieldFactory addClassConstructor(Class<E> cls, IClassConstructor<E> constructFunc) {
        this.predefinedClassConstruct.put(cls, constructFunc);
        return this;
    }

    public FieldFactory addImmutableType(Class... cls) {
        for (Class cl : cls) {
            this.immutableTypes.add(cl);
        }
        return this;
    }

    public boolean isImmutable(Class cls) {
        return cls.isPrimitive() || this.immutableTypes.contains(cls);
    }

    public ReflectNode newReflectNode(Object ob) {
        return new ReflectNode(this, ob.getClass().getSimpleName(), null, ob, ob.getClass(), this.newReferenceCounter());
    }

    public <E> ReferenceCounter<E> newReferenceCounter() {
        return new ReferenceCounter<E>(cls -> !(this.isImmutable(cls) || cls.isEnum()));
    }

}
