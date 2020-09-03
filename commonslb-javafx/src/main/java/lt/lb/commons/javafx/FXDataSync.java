package lt.lb.commons.javafx;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.extractors.Extractors;
import lt.lb.commons.func.Converter;

/**
 *
 * @author laim0nas100
 */
public class FXDataSync extends Extractors {

    public static class ProxyPropertyConverter<T> implements Converter<ValueProxy<T>, Property<T>> {

        @Override
        public Property<T> getFrom(ValueProxy<T> from) {
            SimpleObjectProperty<T> prop = new SimpleObjectProperty<T>() {
                @Override
                public T get() {
                    return from.get();
                }

                @Override
                public T getValue() {
                    return from.getValue();
                }

            };

            prop.set(from.get());
            prop.addListener((FXDefs.SimpleChangeListener<T>) newValue -> {
                from.set(newValue);
            });

            return prop;

        }

        @Override
        public ValueProxy<T> getBackFrom(Property<T> to) {
            return quickProxy(to::getValue, to::setValue);
        }

    }

    public static <T> ValueProxy<T> toProxy(Property<T> prop) {
        return new ProxyPropertyConverter<T>().getBackFrom(prop);
    }
}
