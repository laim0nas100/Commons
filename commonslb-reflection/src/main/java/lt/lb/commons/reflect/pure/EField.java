package lt.lb.commons.reflect.pure;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import lt.lb.commons.F;
import lt.lb.commons.containters.caching.LazyValue;
import lt.lb.commons.reflect.FieldHolder;

/**
 * Effective field. Lazy population class field representation with shadowing, from PureREflectNode.
 *
 * @author laim0nas100
 */
public interface EField {

    public static EField fromComposite(PureReflectNode parent, String name, PureReflectNode node) {
        LazyValue<Field> f = new LazyValue<>(() -> {
            return parent.getLocalCompositeFields().get(name);
        });
        LazyValue<EField> shadowed = new LazyValue<>(() -> {
            PureReflectNode superNode = node.getSuperNode();
            if (superNode == null) {
                return null;
            }
            return EField.fromComposite(parent, name, superNode);
        });

        LazyValue<Collection<EField>> children = new LazyValue<>(() -> {
            FieldHolder.FieldMap localCompositeFields = node.getLocalCompositeFields();
            FieldHolder.FieldMap localPrimitiveOrWrapperFields = node.getLocalPrimitiveOrWrapperFields();
            ArrayList<EField> list = new ArrayList<>(localCompositeFields.size() + localPrimitiveOrWrapperFields.size());
            F.iterate(node.compositeChildNodes.get(), (fname, cnode) -> {
                list.add(EField.fromComposite(node, fname, cnode));
            });
            F.iterate(node.getLocalPrimitiveOrWrapperFields(), (fname, field) -> {
                list.add(EField.fromPrimitive(node, fname));
            });
            return list;
        });
        return new EField() {

            @Override
            public Field getField() {
                return f.get();
            }

            @Override
            public EField getShadowed() {
                return shadowed.get();
            }

            @Override
            public Collection<EField> getChildren() {
                return children.get();
            }
        };
    }

    public static EField fromPrimitive(PureReflectNode parent, String name) {
        LazyValue<Field> f = new LazyValue<>(() -> {
            return parent.getLocalPrimitiveOrWrapperFields().get(name);
        });
        LazyValue<EField> shadowed = new LazyValue<>(() -> {
            PureReflectNode superNode = parent.getSuperNode();
            if (superNode == null) {
                return null;
            }
            FieldHolder.FieldMap localPrimitiveOrWrapperFields = superNode.getLocalPrimitiveOrWrapperFields();
            if (localPrimitiveOrWrapperFields.containsKey(name)) {
                return EField.fromPrimitive(superNode, name);
            } else {
                return null;
            }

        });
        return new EField() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isComposite() {
                return false;
            }

            @Override
            public Field getField() {
                return f.get();
            }

            @Override
            public EField getShadowed() {
                return shadowed.get();
            }

            @Override
            public Collection<EField> getChildren() {
                return new LinkedList<>();
            }
        };
    }

    public static EField fromCompositeRoot(PureReflectNode root) {

        LazyValue<Collection<EField>> children = new LazyValue<>(() -> {
            FieldHolder.FieldMap localCompositeFields = root.getLocalCompositeFields();
            FieldHolder.FieldMap localPrimitiveOrWrapperFields = root.getLocalPrimitiveOrWrapperFields();
            ArrayList<EField> list = new ArrayList<>(localCompositeFields.size() + localPrimitiveOrWrapperFields.size());
            F.iterate(root.compositeChildNodes.get(), (fname, cnode) -> {
                list.add(EField.fromComposite(root, fname, cnode));
            });
            F.iterate(root.getLocalPrimitiveOrWrapperFields(), (fname, field) -> {
                list.add(EField.fromPrimitive(root, fname));
            });
            return list;
        });

        LazyValue<EField> shadowed = new LazyValue<>(() -> {
            if (root.getSuperNode() != null) {
                return EField.fromCompositeRoot(root.getSuperNode());
            }
            return null;
        });
        return new EField() {
            @Override
            public Field getField() {
                return null;
            }

            @Override
            public boolean isComposite() {
                return true;
            }

            @Override
            public Class getType() {
                return root.getFromClass();
            }

            @Override
            public EField getShadowed() {
                return shadowed.get();
            }

            @Override
            public Collection<EField> getChildren() {
                return children.get();
            }
        };
    }

    public Field getField();

    public default String getName() {
        if (this.getField() != null) {
            return this.getField().getName();
        }
        return null;
    }

    public default Class getType() {
        return getField().getType();
    }

    public EField getShadowed();

    public Collection<EField> getChildren();

    public default boolean isComposite() {
        return !getChildren().isEmpty();
    }

}
