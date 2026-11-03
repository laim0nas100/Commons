package lt.lb.zk.builder;

import com.github.laim0nas100.commonslb.containers.collections.Props;
import com.github.laim0nas100.commonslb.parsing.StringParserWithDefaultValue;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class Prop<T> implements Props.PropGet<String, String>, StringParserWithDefaultValue<String, T> {

    public T defaultVal;
    public boolean optional;
    public Props<String> props;
    public String key;

    @Override
    public T getDefault() {
        if (defaultVal == null) {
            throw new IllegalArgumentException("No default value for " + this.getKey());
        }

        return defaultVal;
    }

    public void set(Object obj) {
        String string = obj == null ? null : String.valueOf(obj);
        insert(props, string);
    }

    public boolean isOptional() {
        return optional;
    }

    public SafeOpt<Prop<T>> getSafe() {
        return SafeOpt.of(this);
    }

    @Override
    public SafeOpt<String> parseOptString(String p) {
        return SafeOpt.ofNullable(p);
    }

    @Override
    public String get() {
        return props.getString(getKey());
    }

    public Prop(String propKey) {
        this(propKey, true);
    }

    public Prop(String propKey, boolean optional) {
        this.key = propKey;
        this.optional = optional;
    }

    @Override
    public String getKey() {
        return key;
    }
}
