package lt.lb.commons.reflect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.For;
import lt.lb.commons.reflect.nodes.ReflectNode;
import lt.lb.commons.reflect.nodes.RootReflectNode;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 */
public abstract class FieldFactory {

    public ILineAppender log = ILineAppender.empty;
    public static final Class[] NUMBER_TYPES = {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};
    public static final Class[] DATE_TYPES = {LocalDate.class, LocalTime.class, LocalDateTime.class};
    public static final Class[] OTHER_IMMUTABLE_TYPES = {String.class, UUID.class, Pattern.class, BigDecimal.class, BigInteger.class};
    public static final Class[] WRAPPER_TYPES = ArrayUtils.addAll(NUMBER_TYPES, Boolean.class, Character.class);
    public static final Class[] JVM_IMMUTABLE_TYPES = ArrayOp.merge(WRAPPER_TYPES, OTHER_IMMUTABLE_TYPES, DATE_TYPES);
    private static final HashSet<Class> JVM_IMMUTABLE_SET = new HashSet<>(Arrays.asList(JVM_IMMUTABLE_TYPES));
    public static final Predicate<Class> isJVMImmutable = (Class cls) -> {
        if (cls.isPrimitive()) {
            return true;
        }
        return ArrayUtils.contains(JVM_IMMUTABLE_TYPES, cls);
    };

    protected IFieldResolver makeImmutableFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> f.set(parentObject, f.get(sourceObject));
    }

    protected IFieldResolver makeImmutableArrayFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {
            Class compType = f.getType().getComponentType();
//            log.appendLine("Array of Immutable ", compType.getName());
            Object sourceArray = f.get(sourceObject);
            if (refCounter.containsKey(sourceArray)) {
//                log.appendLine("Found repeating immutable array reference");
                f.set(parentObject, refCounter.get(sourceArray));
            } else {
                int length = Array.getLength(sourceArray);
                Object array = java.lang.reflect.Array.newInstance(compType, length);

                System.arraycopy(sourceArray, 0, array, 0, length);
                f.set(parentObject, array);
                refCounter.computeIfAbsent(sourceArray, k->array);
            }

        };
    }

    protected IFieldResolver makeEnumFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {
            f.set(parentObject, cloneEnum(f.get(sourceObject)));
        };
    }

    protected IFieldResolver makeDeferedFieldResolver(Field f) {
        return (Object sourceObject, Object parentObject, IdentityHashMap refCounter) -> {
//            log.appendLine("In defered resolver", f);

//            log.appendLine("Try to get instance from " + sourceObject.getClass().getName());
            Object get = f.get(sourceObject);

            if (get == null) {// our job is easy
//                log.appendLine("Got null");
                f.set(parentObject, null);
                return;
            }
            Class realFieldType = get.getClass();
            IFieldResolver refinedResolver = getResolverByField(realFieldType, f, false);

//            log.appendLine("Got instance", get, realFieldType);
            refinedResolver.cloneField(sourceObject, parentObject, refCounter);
        };
    }

    protected IFieldResolver makeMutableArrayFieldResolver(Class compType, Field f) {
        return (Object source, Object parentObject, IdentityHashMap refCounter) -> {
            Object[] sourceArray = (Object[]) f.get(source);

            if (refCounter.containsKey(sourceArray)) {
//                log.appendLine("Found repeating " + compType.getName() + " array reference");
                f.set(parentObject, refCounter.get(sourceArray));
            } else {
                if (sourceArray == null) {
//                    log.appendLine("Found null mutable array");
                    f.set(parentObject, null);
                    return;
                }
                int length = Array.getLength(sourceArray);
                Object[] newArray = ArrayOp.makeArray(length, compType);

                final boolean isInitializable = isInitializableMutable(compType);

                for (int i = 0; i < length; i++) {
                    Object sourceInstance = sourceArray[i];

                    Object newInstance = null;

                    if (sourceInstance != null) {
                        IFieldResolver rr = null;

                        if (compType.isEnum()) {
                            newInstance = cloneEnum(sourceInstance);
                        } else {
                            if (isInitializable) {
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
                refCounter.computeIfAbsent(sourceArray, k->newArray);
            }
        };
    }

    private FieldHolder getFieldHolder(Class cls) {
        if (this.useFieldHolderCache) {
            return this.cacheOfFieldHolders.get(cls, k -> new FieldHolder(k));
        } else {
            return new FieldHolder(cls);
        }
    }

    private Map<String, List<Field>> getAllFieldsWithShadowing(Class startingClass) {
        Map<String, List<Field>> fields = new HashMap<>();
        Class currentClass = startingClass;
        while (currentClass != null) {
            FieldHolder holder = this.getFieldHolder(currentClass);
            For.entries().iterate(holder.getFields(), (k, v) -> {
                fields.computeIfAbsent(k, key -> new LinkedList<>()).add(v);
            });
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    protected IFieldResolver makeCompositeFieldResolver(Class fieldType, Field f) {
        FieldFactory me = this;
        return (Object sourceObject, Object parentObject, IdentityHashMap refCounter) -> {
            //TODO maybe let call clone() if object is clonable?

//            log.appendLine("Try get source", fieldType.getName(), f);
            Object sourceInstance = f.get(sourceObject);
//            log.appendLine("Got source", sourceInstance);

            if (refCounter.containsKey(sourceInstance)) {
//                log.appendLine("Found repeating reference");
                f.set(parentObject, refCounter.get(sourceInstance));

            } else {
                if (predefinedClone.containsKey(fieldType)) {
//                    log.appendLine("Found predefined clone of " + fieldType.getName());
                    Object clonedValue = predefinedClone.get(fieldType).clone(me, sourceInstance);
                    f.set(parentObject, clonedValue);
                    return;
                }
                if (sourceInstance == null) {
//                    log.appendLine("Found null", f);
                    f.set(parentObject, null);
                } else {
//                    log.appendLine("Basic clone ", f);
                    Class realClass = fieldType;

                    if (!isInitializableMutable(realClass)) {
                        realClass = sourceObject.getClass();
                    }
                    Object newInstance = createNewInstance(realClass);
                    refCounter.computeIfAbsent(sourceInstance, k->newInstance);
//                    refCounter.registerIfAbsent(sourceInstance, newInstance);

                    IFieldResolver rr = recursiveResolver(realClass);

                    //recursive downward clone
                    rr.cloneField(sourceInstance, newInstance, refCounter);

                    f.set(parentObject, newInstance);
//                    log.appendLine("Succesfull set", f);
                }

            }
        };
    }

    public static Object defaultPrimitive(Class cls) {
        if (!cls.isPrimitive() || ArrayOp.count(Predicate.isEqual(cls), WRAPPER_TYPES) >= 1) {
            throw new IllegalArgumentException(cls.getName() + " is not a primitive");
        } else {
            if (Byte.TYPE.equals(cls)) {
                return (byte) 0x00;
            }
            if (Short.TYPE.equals(cls)) {
                return (short) 0;
            }
            if (Integer.TYPE.equals(cls)) {
                return 0;
            }
            if (Long.TYPE.equals(cls)) {
                return 0L;
            }
            if (Float.TYPE.equals(cls)) {
                return 0F;
            }
            if (Double.TYPE.equals(cls)) {
                return 0D;
            }
            if (Character.TYPE.equals(cls)) {
                return (char) 0;
            }
            return null;
        }
    }

    public static Object defaultPrimitiveWrapper(Class cls) {
        if (ArrayUtils.contains(WRAPPER_TYPES, cls)) {
            if (Byte.class.equals(cls)) {
                return (byte) 0x00;
            }
            if (Short.class.equals(cls)) {
                return (short) 0;
            }
            if (Integer.class.equals(cls)) {
                return 0;
            }
            if (Long.class.equals(cls)) {
                return 0L;
            }
            if (Float.class.equals(cls)) {
                return 0F;
            }
            if (Double.class.equals(cls)) {
                return 0D;
            }
            if (Character.class.equals(cls)) {
                char c = 0;
                return c;
            }
            return null;
        } else {
            throw new IllegalArgumentException(cls.getName() + " is not a wrapper type");
        }
    }

    public static Enum cloneEnum(Object ob) {
        return (Enum) ob;
    }

    public <T> T createNewInstance(Class<T> cls) {

        //maybe cache class constructors also?
        //check class constructors
        if (this.predefinedClassConstructors.containsKey(cls)) {
            return (T) this.predefinedClassConstructors.get(cls).construct();
        }
        if (this.cacheConstructors) {
            IClassConstructor ifPresent = this.cacheOfClassConstructors.getIfPresent(cls);
            if (ifPresent != null) {
                return (T) ifPresent.construct();
            }
        }

        boolean isAbstract = Modifier.isAbstract(cls.getModifiers());
        if (isAbstract) {
            throw new IllegalArgumentException("Can't initialize an abstract class " + cls.getName());
        }
        if (cls.isInterface()) {
            throw new IllegalArgumentException("Can't initialize an interface " + cls.getName() + " pass implementation class");
        }
        if (cls.isEnum()) {
            throw new IllegalArgumentException("Can't initialize an enum " + cls.getName());
        }
        if (cls.isArray()) {
            throw new IllegalArgumentException("Can't initialize an array " + cls.getName() + " consider using Array::newInstance");
        }
        Object newInstance = null;

        try {
            newInstance = cls.getDeclaredConstructor().newInstance();
//            log.appendLine("Easy instantiation " + cls.getName());
            return (T) newInstance;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        } 

        Constructor<?>[] declaredConstructors = cls.getDeclaredConstructors();
        // sort by lower parameter constructor first
        Arrays.sort(declaredConstructors, (o1, o2) -> o1.getParameterCount() - o2.getParameterCount());

        for (Constructor<?> cons : declaredConstructors) {
            cons.setAccessible(true);

            int argCount = cons.getParameterCount();
            Class[] parameterTypes = cons.getParameterTypes();
            Object[] constArray = new Object[argCount];
            try {
                for (int j = 0; j < parameterTypes.length; j++) {
                    Class type = parameterTypes[j];
                    if (parameterTypes[j].isPrimitive()) {
                        constArray[j] = defaultPrimitive(type);
                    } else if (ArrayOp.count(Predicate.isEqual(type), FieldFactory.WRAPPER_TYPES) >= 1) {
                        constArray[j] = defaultPrimitiveWrapper(type);
                    }//else leave null

                }

//                log.appendLine("Try with " + cons);
//                log.appendLine(constArray);
                newInstance = cons.newInstance(constArray);

                if (newInstance != null) {
//                    log.appendLine("We good");
                    if (this.cacheConstructors) {
                        this.cacheOfClassConstructors.put(cls, () -> {
                            try {
                                return cons.newInstance(constArray);
                            } catch (Exception e) {
                                throw new RuntimeException(cons.toString() + " has failed now, but was good before?. Should not happen");
                            }
                        });
                    }
                    // we good
                    return (T) newInstance;
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            }
        }

        if (this.unsafeAllocator != null) {
            // THE USAFE
//            log.appendLine("Try with unsafe");
            newInstance = this.unsafeAllocator.apply(cls);
            if (this.cacheConstructors) {
                this.cacheOfClassConstructors.put(cls, () -> this.unsafeAllocator.apply(cls));
            }
            return (T) newInstance;
        }

        throw new IllegalStateException("Failed to instantiate class " + cls.getName() + " consider adding IClassConstructor");
    }

    public boolean isInitializableMutable(Class cls) {
        if (cls.isEnum() || cls.isInterface() || cls.isPrimitive() || cls.isArray()) {
            return false;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        return !this.isImmutable(cls);
    }

    private IFieldResolver recursiveResolver(final Class startingClass) {
        if (!this.useCache) {
            return this.recursiveResolverCached(startingClass);
        }
        return this.cacheOfFieldResolvers.get(startingClass, (val) -> recursiveResolverCached(startingClass));
    }

    private IFieldResolver getResolverByField(Class fieldType, Field f, boolean defer) {
//        log.appendLine("Get resolver by field", fieldType, f);

        Supplier<IFieldResolver> frSupp = () -> {

            IFieldResolver fr = null;
            boolean isSingular = !fieldType.isArray();
            if (isSingular) {

                if (fieldType.isEnum()) {
//                    log.appendLine("is enum", f);
                    fr = makeEnumFieldResolver(f);
//                maybeAddToCache(fieldType,f,fr);
                } else if (isImmutable(fieldType)) {
//                    log.appendLine("is immutable", f);
                    fr = makeImmutableFieldResolver(f);
                } else if (defer) {
//                    log.appendLine("Explicit defer");
                    fr = makeDeferedFieldResolver(f);
                } else if (this.isInitializableMutable(fieldType)) {
//                    log.appendLine("is composite", f);
                    fr = this.makeCompositeFieldResolver(fieldType, f);
//                maybeAddToCache(fieldType,f,fr);
                } else {

                    throw new IllegalStateException("Couldn't resolve type for field" + f);
//                fr = makeDeferedFieldResolver(f);

                }
            } else { // is array
                final Class compType = fieldType.getComponentType();
//                log.appendLine("Array of " + compType.getName());
                if (isImmutable(compType)) {
                    fr = makeImmutableArrayFieldResolver(f);
//                maybeAddToCache(fieldType,f,fr);
                } else {
                    fr = makeMutableArrayFieldResolver(compType, f);
//                maybeAddToCache(fieldType,f,fr);
                }
            }

            return fr;
        };

        //TODO maybe cache primitive resolvers, with [class + fieldName]?
        if (this.useFieldCache && !defer) {
            return this.cacheOfFields.get(f, k -> frSupp.get());
        }
        return frSupp.get();

    }

    private IFieldResolver recursiveResolverCached(final Class startingClass) {
        Map<String, IFieldResolver> resolverMap = new HashMap<>();
        For.entries().iterate(this.getAllFieldsWithShadowing(startingClass), (key, list) -> {
            IFieldResolver finalResolver = IFieldResolver.empty();
            for (Field f : list) {
                f.setAccessible(true);
                finalResolver = finalResolver.nest(getResolverByField(f.getType(), f, true));
            }
            resolverMap.put(key, finalResolver);
        });

        return (Object source, Object parentObject, IdentityHashMap refCounter) -> {
            for (Map.Entry<String, IFieldResolver> resolverEntry : resolverMap.entrySet()) {
//                log.appendLine("Do clone ", resolverEntry.getKey());
                resolverEntry.getValue().cloneField(source, parentObject, refCounter);
            }
        }; // combine fields from map

    }

    protected Map<Class, IExplicitClone> predefinedClone = new HashMap<>();
    protected Map<Class, IClassConstructor> predefinedClassConstructors = new HashMap<>();
    protected Set<Class> immutableTypes = new HashSet<>();

    protected Function<Class, ?> unsafeAllocator = UnsafeProvider.getUnsafeAllocator();
    protected Cache<Field, IFieldResolver> cacheOfFields = Caffeine.newBuilder().build();
    protected Cache<Class, FieldHolder> cacheOfFieldHolders = Caffeine.newBuilder().build();
    protected Cache<Class, IFieldResolver> cacheOfFieldResolvers = Caffeine.newBuilder().build();
    protected Cache<Class, IClassConstructor> cacheOfClassConstructors = Caffeine.newBuilder().build();

    public boolean useFieldHolderCache = true;
    public boolean useCache = true;
    public boolean useFieldCache = true;
    public boolean cacheConstructors = true;

    public <T> T reflectionClone(T source) throws Exception {
        Class<T> cls = (Class<T>) source.getClass();
        T newInstance = createNewInstance(cls);

        IFieldResolver recursiveResolver = recursiveResolver(cls);
        recursiveResolver.cloneField(source, newInstance, new IdentityHashMap(8));
        return newInstance;
    }

    public <T extends Object> T fastClone(T source) throws Exception {
        if (source instanceof Cloneable) {
            Method method = source.getClass().getMethod("clone");
            boolean access = method.isAccessible();
            if (!access) {
                method.setAccessible(true);
            }

            Object cloned = method.invoke(source);
            if (!access) {
                method.setAccessible(false);
            }
            return (T) cloned;
        }
        throw new IllegalArgumentException(source + " is not marked as Cloneable");
    }

    public <E> FieldFactory addExplicitClone(Class<E> cls, IExplicitClone<E> cloneFunc) {
        this.predefinedClone.put(cls, cloneFunc);
        return this;
    }

    public <E> FieldFactory addClassConstructor(Class<E> cls, IClassConstructor<E> constructFunc) {
        this.predefinedClassConstructors.put(cls, constructFunc);
        return this;
    }

    public FieldFactory addImmutableType(Class... cls) {
        this.immutableTypes.addAll(Arrays.asList(cls));
        return this;
    }

    public boolean isImmutable(Class cls) {
        return cls.isPrimitive() || this.immutableTypes.contains(cls);
    }

    public ReflectNode newReflectNode(Object ob) {
        return new RootReflectNode(this, ob.getClass().getSimpleName(), null, ob, ob.getClass(), this.newReferenceCounter());
    }

    public <E> ReferenceCounter<E> newReferenceCounter() {
        return new ReferenceCounter<>(cls -> !(this.isImmutable(cls) || cls.isEnum()));
    }

}
