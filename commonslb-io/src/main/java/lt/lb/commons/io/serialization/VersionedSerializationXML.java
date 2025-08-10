package lt.lb.commons.io.serialization;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.io.serialization.VersionedSerialization.*;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializationXML {

    public static enum XMLEncoding {

        ENC1_0("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), ENC1_1("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");

        public final String header;

        XMLEncoding(String header) {
            this.header = header;
        }
    }
    
    protected XMLEncoding encoding = XMLEncoding.ENC1_1;

    public void writeWithEncodingHeader(Appendable writer, VSUnit unit) throws IOException {
        writer.append(encoding.header);
        writeGeneric(writer, unit);
    }

    public void writeGeneric(Appendable writer, VSUnit unit) throws IOException {
        boolean hasChildren = false;
        boolean hasValues = hasValues(unit);
        Iterable<? extends VSUnit> children = ImmutableCollections.listOf();
        if (unit instanceof VSChildren) {
            VSChildren childrenParent = F.cast(unit);
            children = childrenParent.children();
            boolean hasNext = children.iterator().hasNext();
            if (hasNext) {
                hasChildren = true;
            }
        }

        writeTagAndAttributes(writer, unit, !hasChildren && !hasValues);

        if (hasChildren || hasValues) {
            for (VSUnit child : children) {
                writeGeneric(writer, child);
            }
            if (hasValues) {
                writeValues(writer, unit);
            }
            writeEndTag(writer, unit);
        }

    }

    protected boolean hasValues(VSUnit unit) {
        if (unit instanceof VSTrait) {
            VSTrait traits = F.cast(unit);
            return traits.hasTrait(VSTraitEnum.VALUE) || traits.hasTrait(VSTraitEnum.BINARY);
        }
        return false;
    }
    
    protected String escapeString(Object str){
        switch (encoding) {
            case ENC1_0:
                return StringEscapeUtils.escapeXml10(String.valueOf(str));
                case ENC1_1:
                return StringEscapeUtils.escapeXml11(String.valueOf(str));
            default:
                throw new IllegalStateException("Unknown encoding:"+encoding);
        }
        
    }

    protected void writeValues(Appendable writer, VSUnit unit) throws IOException {
        if (unit instanceof VSTrait) {
            VSTrait traits = F.cast(unit);
            if (traits.hasTrait(VSTraitEnum.VALUE)) {
                Object get = traits.traits().get(VSTraitEnum.VALUE);// non binary, so just convert to string
                writer.append('<').append(VSTraitEnum.VALUE.name()).append('>');
                writer.append(escapeString(get));
                writer.append('<').append('/').append(VSTraitEnum.VALUE.name()).append('>');
            }
            if (traits.hasTrait(VSTraitEnum.BINARY)) {
                Object get = traits.traits().get(VSTraitEnum.BINARY);// non binary, so just convert to string
                writer.append('<').append(VSTraitEnum.BINARY.name()).append('>');
                String encodeToString = Base64.getEncoder().encodeToString(F.cast(get));
                writer.append(encodeToString);
                writer.append('<').append('/').append(VSTraitEnum.BINARY.name()).append('>');
            }
        }
    }

    protected void writeEndTag(Appendable writer, VSUnit unit) throws IOException {
        writer.append('<').append('/').append(escapeString(unit.getClass().getSimpleName())).append('>');
    }

    protected void writeTagAndAttributes(Appendable writer, VSUnit unit, boolean empty) throws IOException {
        writer.append('<').append(escapeString(unit.getClass().getSimpleName()));
        Set<Map.Entry<String, String>> entrySet = getAttributes(unit).entrySet();
        for (Map.Entry<String, String> attribute : entrySet) {
            writer.append(' ').append(attribute.getKey()).append('=')
                    .append('"').append(attribute.getValue()).append('"');
        }
        if (empty) {
            writer.append('/').append('>');
        } else {
            writer.append('>');
        }

    }

    protected Map<String, String> getAttributes(VSUnit unit) {
        if (unit instanceof VSTrait) {
            Map<String, String> attributes = new LinkedHashMap<>();
            VSTrait trait = F.cast(unit);
            //only possible attributes to write
            resolveTrait(trait, attributes, VSTraitEnum.TYPE);
            resolveTrait(trait, attributes, VSTraitEnum.COLLECTION_TYPE);
            resolveTrait(trait, attributes, VSTraitEnum.FIELD_NAME);
            resolveTrait(trait, attributes, VSTraitEnum.REF_ID);
            resolveTrait(trait, attributes, VSTraitEnum.VERSION);
            return attributes;
        } else {
            return ImmutableCollections.mapOf();
        }

    }

    protected void resolveTrait(VSTrait trait, Map<String, String> attributes, VSTraitEnum traitEnum) {
        if (trait.hasTrait(traitEnum)) {
            Object get = trait.traits().get(traitEnum);
            attributes.put(traitEnum.name(), escapeString(get));
        }
    }

}
