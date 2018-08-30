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
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Log;
import lt.lb.commons.containers.Tuple;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.misc.F;

/**
 *
 * @author Lemmin
 */
public abstract class FieldFactory {
    
    //TODO clone mutable enums

    private static class CachedFieldResolvers extends ConcurrentHashMap<Tuple<Class, String>, IFieldResolver> {

    }

    public ILineAppender log;

    public static final Class[] NUMBER_TYPES = {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};
    public static final Class[] DATE_TYPES = {LocalDate.class, LocalTime.class, LocalDateTime.class};
    public static final Class[] OTHER_IMMUTABLE_TYPES = {String.class, UUID.class, Pattern.class, BigDecimal.class, BigInteger.class};
    public static final Class[] WRAPPER_TYPES = ArrayOp.merge(NUMBER_TYPES, Boolean.class, Character.class);
    public static final Class[] JVM_IMMUTABLE_TYPES = ArrayOp.merge(WRAPPER_TYPES, OTHER_IMMUTABLE_TYPES, DATE_TYPES);
    public static final Predicate<Class> isJVMImmutable = (Class cls) -> {
        if (cls.isPrimitive()) {
            return true;
        }
        return (ArrayOp.count(Predicate.isEqual(cls), JVM_IMMUTABLE_TYPES) > 0);
    };

    protected IFieldResolver makeImmutableFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> f.set(parentObject, f.get(sourceObject));
    }

    protected IFieldResolver makeImmutableArrayFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {

            Class compType = f.getType().getComponentType();
            log.appendLine("Array of Immutable ", compType.getName());
            Object sourceArray = f.get(sourceObject);
            boolean needRegistration = false;
            if (refCounter.contains(sourceArray)) {
                log.appendLine("Found repeating immutable array reference");
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

    protected IFieldResolver makeEnumFieldResolver(Field f) {
        return (sourceObject, parentObject, refCounter) -> {
            f.set(parentObject, cloneEnum(f.get(sourceObject)));
        };
    }

    protected IFieldResolver makeDeferedFieldResolver(Field f) {
        return new IFieldResolver() {
            @Override
            public void cloneField(Object sourceObject, Object parentObject, ReferenceCounter refCounter) throws Exception {
                log.appendLine("In defered resolver", f);

                log.appendLine("Try to get instance from " + sourceObject.getClass().getName());
                Object get = f.get(sourceObject);

                if (get == null) {// our job is easy
                    log.appendLine("Got null");
                    f.set(parentObject, null);
                    return;
                }
                Class realFieldType = get.getClass();
                log.appendLine("Got instance", get, realFieldType);
                IFieldResolver refinedResolver = getResolverByField(realFieldType, f);

                refinedResolver.cloneField(sourceObject, parentObject, refCounter);

            }
        };
    }

    protected IFieldResolver makeMutableArrayFieldResolver(Class compType, Field f) {
        return (Object source, Object parentObject, ReferenceCounter refCounter) -> {
            Object[] sourceArray = (Object[]) f.get(source);

            if (refCounter.contains(sourceArray)) {
                log.appendLine("Found repeating " + compType.getName() + " array reference");
                f.set(parentObject, refCounter.get(sourceArray));
            } else {

                int length = Array.getLength(sourceArray);
                Object[] newArray = ArrayOp.makeArray(length, compType);

                final boolean isInitializable = isInitializable(compType);

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
                refCounter.registerIfAbsent(sourceArray, newArray);
            }
        };
    }

    private FieldHolder getFieldHolder(Class cls) {
        FieldHolder holder = null;
        if (this.useFieldHolderCache) {
            if (this.cachedFieldHolders.containsKey(cls)) {
                holder = this.cachedFieldHolders.get(cls);
            } else {
                holder = new FieldHolder(cls);
                this.cachedFieldHolders.put(cls, holder);
            }
        } else {
            holder = new FieldHolder(cls);
        }
        return holder;
    }

    private Map<String, List<Field>> getAllFieldsWithShadowing(Class startingClass) {
        Map<String, List<Field>> fields = new HashMap<>();
        Class currentClass = startingClass;
        while (currentClass != null) {
            FieldHolder holder = this.getFieldHolder(currentClass);
            F.iterate(holder.getFields(), (k,v)->{
                if(fields.containsKey(k)){
                    fields.get(k).add(v);
                }else{
                    LinkedList<Field> list = new LinkedList<>();
                    list.add(v);
                    fields.put(k, list);
                }
            });
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    protected IFieldResolver makeCompositeFieldResolver(Class fieldType, Field f) {
        FieldFactory me = this;
        return (Object sourceObject, Object parentObject, ReferenceCounter refCounter) -> {
            //TODO maybe let call clone() if object is clonable?

            log.appendLine("Try get source", fieldType.getName(), f);
            Object sourceInstance = f.get(sourceObject);
            log.appendLine("Got source", sourceInstance);

            if (refCounter.contains(sourceInstance)) {
                log.appendLine("Found repeating reference");
                f.set(parentObject, refCounter.get(sourceInstance));

            } else {
                if (predefinedClone.containsKey(fieldType)) {
                    log.appendLine("Found predefined clone of " + fieldType.getName());
                    Object clonedValue = predefinedClone.get(fieldType).clone(me, sourceInstance);
                    f.set(parentObject, clonedValue);
                    return;
                }
                if (sourceInstance == null) {
                    log.appendLine("Found null", f);
                    f.set(parentObject, null);
                } else {
                    log.appendLine("Basic clone ", f);
                    Class realClass = fieldType;

                    if (!isInitializable(realClass)) {
                        realClass = sourceObject.getClass();
                    }
                    Object newInstance = createNewInstance(realClass);
                    refCounter.registerIfAbsent(sourceInstance, newInstance);

                    IFieldResolver rr = recursiveResolver(realClass);

                    //recursive downward clone
                    rr.cloneField(sourceInstance, newInstance, refCounter);

                    f.set(parentObject, newInstance);
                    log.appendLine("Succesfull set", f);
                }

            }
        };
    }

    public static Object defaultPrimitive(Class cls) {
        if (!cls.isPrimitive() || ArrayOp.count(Predicate.isEqual(cls), WRAPPER_TYPES)>=1) {
            throw new IllegalArgumentException(cls.getName() + " is not a primitive");
        }else{
            if(Byte.TYPE.equals(cls)){
                return (byte)0x00;
            }
            if(Short.TYPE.equals(cls)){
                return (short)0;
            }
            if(Integer.TYPE.equals(cls)){
                return 0;
            }
            if(Long.TYPE.equals(cls)){
                return 0L;
            }
            if(Float.TYPE.equals(cls)){
                return 0F;
            }
            if(Double.TYPE.equals(cls)){
                return 0D;
            }
            if(Character.TYPE.equals(cls)){
                char c = 0;
                return c;
            }
            return null;
        }
    }
    
    public static Object defaultPrimitiveWrapper(Class cls){
        if(ArrayOp.count(Predicate.isEqual(cls), WRAPPER_TYPES) >= 1){
            if(Byte.class.equals(cls)){
                return (byte)0x00;
            }
            if(Short.class.equals(cls)){
                return (short)0;
            }
            if(Integer.class.equals(cls)){
                return 0;
            }
            if(Long.class.equals(cls)){
                return 0L;
            }
            if(Float.class.equals(cls)){
                return 0F;
            }
            if(Double.class.equals(cls)){
                return 0D;
            }
            if(Character.class.equals(cls)){
                char c = 0;
                return c;
            }
            return null;
        }else{
            throw new IllegalArgumentException(cls.getName() + " is not a wrapper type");
        }
    }

    public static Enum cloneEnum(Object ob) {
        Enum en = (Enum) ob;
        Class<Enum> cls = (Class<Enum>) ob.getClass();
        return Enum.valueOf(cls, en.name());
    }

    public <T> T createNewInstance(Class<T> cls) {
        
        //maybe cache class constructors also?

        //check class constructors
        if (this.predefinedClassConstructors.containsKey(cls)) {
            return (T) this.predefinedClassConstructors.get(cls).construct();
        }
        if(this.cacheConstructors && this.cachedClassConstructors.containsKey(cls)){
            return (T) this.cachedClassConstructors.get(cls).construct();
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
            log.appendLine("Easy instantiation " + cls.getName());
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
            Class[] parameterTypes = cons.getParameterTypes();
            Object[] constArray = new Object[argCount];
            try {
                for (int j = 0; j < parameterTypes.length; j++) {
                    Class type = parameterTypes[j];
                    if(parameterTypes[j].isPrimitive()){
                        constArray[j] = defaultPrimitive(type);
                    }else if(ArrayOp.count(Predicate.isEqual(type), FieldFactory.WRAPPER_TYPES) >= 1){
                        constArray[j] = defaultPrimitiveWrapper(type);
                    }//else leave null
                    
                }

                log.appendLine("Try with " + cons);
                log.appendLine(constArray);
                newInstance = cons.newInstance(constArray);

                if (newInstance != null) {
                    log.appendLine("We good");
                    if(this.cacheConstructors){
                        this.cachedClassConstructors.put(cls, (IClassConstructor) () -> {
                            try{
                                return cons.newInstance(constArray);
                            }catch(Exception e){
                                throw new RuntimeException(cons.toString() +" has failed now, but was good before??1!");
                            }
                        });
                    }
                    // we good
                    return (T) newInstance;
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Failed to instantiate class " + cls.getName() + " consider adding ClassConstructor");
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
        if (!this.useCache) {
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

    private void maybeAddToCache(Class cls, Field f, IFieldResolver fr) {
        if (this.useFieldCache) {
            Tuple key = new Tuple(cls, f.getName());
            this.cachedExactFieldResolvers.putIfAbsent(key, fr);
        }
    }

    private IFieldResolver getResolverByField(Class fieldType, Field f) {
        log.appendLine("Get resolver by field", fieldType, f);

//        Tuple<Class,String> key = null;
//        if(this.useFieldCache){
//            key = new Tuple<>(fieldType,f.getName());
//            if(this.cachedExactFieldResolvers.containsKey(key)){
//                return this.cachedExactFieldResolvers.get(key);
//            }
//        }
        //TODO maybe cache primitive resolvers, with [class + fieldName]?
        IFieldResolver fr = null;
        boolean isSingular = !fieldType.isArray();
        if (isSingular) {
            if (fieldType.isEnum()) {
                log.appendLine("is enum", f);
                fr = makeEnumFieldResolver(f);
//                maybeAddToCache(fieldType,f,fr);
            } else if (isImmutable(fieldType)) {
                log.appendLine("is immutable", f);
                fr = makeImmutableFieldResolver(f);
            } else if (this.isInitializable(fieldType)) {
                log.appendLine("is composite", f);
                fr = this.makeCompositeFieldResolver(fieldType, f);
//                maybeAddToCache(fieldType,f,fr);
            } else {
                log.appendLine("defered resolver", f);
                fr = makeDeferedFieldResolver(f);

            }
        } else { // is array
            final Class compType = fieldType.getComponentType();
            log.appendLine("Array of " + compType.getName());
            if (isImmutable(compType)) {
                fr = makeImmutableArrayFieldResolver(f);
//                maybeAddToCache(fieldType,f,fr);
            } else {
                fr = makeMutableArrayFieldResolver(compType, f);
//                maybeAddToCache(fieldType,f,fr);
            }
        }

        return fr;
    }

    private IFieldResolver recursiveResolverCached(final Class startingClass) {
        Map<String, IFieldResolver> resolverMap = new HashMap<>();
        F.iterate(this.getAllFieldsWithShadowing(startingClass), (key,list)->{
            IFieldResolver finalResolver = IFieldResolver.empty();
            for(Field f:list){
                f.setAccessible(true);
                finalResolver = finalResolver.nest(getResolverByField(f.getType(), f));
                
            }
            resolverMap.put(key, finalResolver);
        });


        return (Object source, Object parentObject, ReferenceCounter refCounter) -> {
            for (Map.Entry<String, IFieldResolver> resolverEntry : resolverMap.entrySet()) {
                log.appendLine("Do clone ", resolverEntry.getKey());
                resolverEntry.getValue().cloneField(source, parentObject, refCounter);
            }
        }; // combine fields from map

    }

    protected Map<Class, IExplicitClone> predefinedClone = new HashMap<>();
    protected Map<Class, IClassConstructor> predefinedClassConstructors = new HashMap<>();
    protected Map<Class, IClassConstructor> cachedClassConstructors = new HashMap<>();
    protected Map<Class, IFieldResolver> cachedFieldResolvers = new HashMap<>();
    protected Map<Class, FieldHolder> cachedFieldHolders = new HashMap<>();
    protected CachedFieldResolvers cachedExactFieldResolvers = new CachedFieldResolvers();
    protected Set<Class> immutableTypes = new HashSet<>();

    public boolean useFieldHolderCache = false;
    public boolean useCache = false;
    public boolean useFieldCache = false;
    public boolean cacheConstructors =true;

    public FieldFactory() {
        LineStringBuilder lsb = new LineStringBuilder();
        Log.instant = true;
        this.log = (objs) -> {
            
            Log.print("FACTORY",lsb.append(objs).clear());
            return log;
        };
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
        this.predefinedClassConstructors.put(cls, constructFunc);
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
        return new RootReflectNode(this, ob.getClass().getSimpleName(), null, ob, ob.getClass(), this.newReferenceCounter());
    }

    public <E> ReferenceCounter<E> newReferenceCounter() {
        return new ReferenceCounter<>(cls -> !(this.isImmutable(cls) || cls.isEnum()));
    }

}
