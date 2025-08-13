package lt.lb.commons.io.serialization;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.io.serialization.VersionedSerialization.*;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.uncheckedutils.SafeOpt;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializationXML {

    static Logger logger = LoggerFactory.getLogger(VersionedSerializationXML.class);

    public static final Map<String, Supplier<VSUnit>> simpleNameToConstructor = MakeStream.from(VersionedSerialization.DEFAULT_CONSTRUCTORS.entrySet())
            .toUnmodifiableMap(e -> e.getKey().getSimpleName(), e -> e.getValue());

    public static enum XMLEncoding {

        ENC1_0("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), ENC1_1("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");

        public final String header;

        XMLEncoding(String header) {
            this.header = header;
        }
    }

    protected XMLEncoding encoding = XMLEncoding.ENC1_0;
    protected SafeOpt<SAXParserFactory> factory = SafeOpt.ofLazy(SAXParserFactory.newInstance()).map(this::configFactory);

    protected SAXParserFactory configFactory(SAXParserFactory fac) {
        fac.setNamespaceAware(true);
        return fac;
    }

    public static class VSXMLContentHandler extends DefaultHandler {

        public static class StackNode {

            public final VSUnit node;
            public final List<VSUnit> children = new ArrayList<>();

            public StackNode(VSUnit node) {
                this.node = node;
            }
        }

        protected ArrayDeque<StackNode> stack = new ArrayDeque<>();
        public VSUnit root;

        protected StringBuilder valueOrBinaryElem = null;

        protected <T extends VSUnit> T current() {
            if (stack.isEmpty()) {
                return null;
            } else {
                return (T) stack.getFirst().node;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {//can be called more than once per element
            if (valueOrBinaryElem == null) {
                throw new SAXException("Unexpected characters value");
            } else {
                valueOrBinaryElem.append(ch, start, length);
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (isValueOrBinary(localName)) {
                if (valueOrBinaryElem == null) {
                    throw new SAXException("Nested elements inside value or binary not expected");
                }
                String value = valueOrBinaryElem.toString();
                valueOrBinaryElem = null;
                VSUnit current = current();
                if (current instanceof TraitBinary) {
                    byte[] decode = Base64.getDecoder().decode(value);
                    TraitBinary binary = F.cast(current);
                    binary.setBinary(decode);
                } else if (current instanceof TraitValue) {
                    //try primitive VSU
                    //EnumVSU and TypedStringVSU are subtypes of StringVSU
                    if (current instanceof StringVSU) {
                        StringVSU cast = F.cast(current);
                        cast.setValue(value);

                    } else if (current instanceof CharVSU) {
                        if (value.length() != 1) {
                            throw new SAXException(CharVSU.class.getSimpleName() + " expects 1 character value");
                        }
                        ((CharVSU) current).setValue(value.charAt(0));
                    } else if (current instanceof IntegerVSU) {
                        ((IntegerVSU) current).setValue(Integer.valueOf(value));
                    } else if (current instanceof LongVSU) {
                        ((LongVSU) current).setValue(Long.valueOf(value));
                    } else if (current instanceof ShortVSU) {
                        ((ShortVSU) current).setValue(Short.valueOf(value));
                    } else if (current instanceof ByteVSU) {
                        ((ByteVSU) current).setValue(Byte.valueOf(value));
                    } else if (current instanceof BooleanVSU) {
                        ((BooleanVSU) current).setValue(Boolean.valueOf(value));
                    } else if (current instanceof FloatVSU) {
                        ((FloatVSU) current).setValue(Float.valueOf(value));
                    } else if (current instanceof DoubleVSU) {
                        ((DoubleVSU) current).setValue(Double.valueOf(value));
                    } else {
                        throw new SAXException("Unhandled unit type:" + current.getClass().getSimpleName());
                    }

                } else {
                    throw new SAXException("Expected value or binary element");
                }

            } else {
                StackNode completedChild = stack.pop();
                List<VSUnit> children = completedChild.children;
                VSUnit unit = completedChild.node;
                //ensure even if children are empty, to make an empty array
                if (unit instanceof EntryVSUnit) {
                    EntryVSUnit cast = F.cast(unit);
                    int size = children.size();
                    if (size != 2) {
                        throw new SAXException(EntryVSUnit.class.getSimpleName() + " expects exactly 2 elements");
                    }
                    cast.key = children.get(0);
                    cast.val = children.get(1);
                } else if (unit instanceof ArrayVSUnit) {
                    ArrayVSUnit cast = F.cast(unit);
                    cast.values = children.stream().toArray(s -> new VSUnit[s]);
                } else if (unit instanceof MapVSUnit) {
                    MapVSUnit cast = F.cast(unit);
                    cast.values = children.stream().toArray(s -> new EntryVSUnit[s]);
                } else if (unit instanceof ComplexVSUnit) {
                    ComplexVSUnit cast = F.cast(unit);
                    cast.fields = children.stream().toArray(s -> new VSField[s]);
                } else if (!children.isEmpty()) {
                    throw new SAXException("unrecognized element with children:" + unit.getClass().getSimpleName());
                }

                StackNode parentNode = stack.peekFirst();
                if (parentNode != null) {// not root node pop
                    parentNode.children.add(completedChild.node);
                } else {//assign root
                    root = completedChild.node;
                }

            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (isValueOrBinary(localName)) {
                if (valueOrBinaryElem != null) {
                    throw new SAXException("Started new value or binary sequance when the old one was not done");
                }
                valueOrBinaryElem = new StringBuilder();
            } else {
                VSUnit unit = createInstance(localName);
                readAttributes(unit, attributes);

                stack.push(new StackNode(unit));
            }
        }

        protected void readAttributes(VSUnit unit, Attributes attributes) throws SAXException {
            if (unit instanceof VSTrait) {
                VSTrait trait = F.cast(unit);
                VSTraits traits = trait.traits();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    String localName = attributes.getLocalName(i);
                    String value = attributes.getValue("", localName);
                    if (localName.equals(VSTraitEnum.FIELD_NAME.name())) {
                        traits.put(VSTraitEnum.FIELD_NAME, value);
                    } else if (localName.equals(VSTraitEnum.TYPE.name())) {
                        traits.put(VSTraitEnum.TYPE, value);
                    } else if (localName.equals(VSTraitEnum.COLLECTION_TYPE.name())) {
                        traits.put(VSTraitEnum.COLLECTION_TYPE, value);
                    } else if (localName.equals(VSTraitEnum.VERSION.name())) {
                        traits.put(VSTraitEnum.VERSION, Long.valueOf(value));
                    } else if (localName.equals(VSTraitEnum.REF_ID.name())) {
                        traits.put(VSTraitEnum.REF_ID, Long.valueOf(value));
                    } else {
                        throw new SAXException("Unhadled attribute:" + localName);
                    }

                }
            }
        }

        public static boolean isValueOrBinary(String elementName) {
            return elementName.equals(VSTraitEnum.BINARY.name()) || elementName.equals(VSTraitEnum.VALUE.name());
        }

        public static void assertValue(String elementName, Class type, VersionedSerialization.VSTraitEnum expectedTrait) throws SAXException {
            if (!expectedTrait.name().equals(elementName)) {
                throw new SAXException(type.getSimpleName() + " expects a " + expectedTrait.name() + " element inside but found:" + elementName);
            }

        }

        protected <T extends VSUnit> T createInstance(String localName) throws SAXException {
            Supplier<VSUnit> type = simpleNameToConstructor.getOrDefault(localName, null);
            if (type == null) {
                throw new SAXException("Unrecognized element:" + localName);
            }
            return (T) type.get();
        }
    }

    public <T extends VSUnit> T readXml(InputSource reader) throws IOException, ParserConfigurationException, SAXException {

        SAXParser saxParser = factory.map(m -> m.newSAXParser())
                .throwIfError(ParserConfigurationException.class)
                .throwIfError(SAXException.class)
                .get();

        XMLReader xmlReader = saxParser.getXMLReader();
        VSXMLContentHandler vsxmlContentHandler = new VSXMLContentHandler();
        xmlReader.setContentHandler(vsxmlContentHandler);
        xmlReader.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                logger.warn("warning:", exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                logger.error("error:", exception);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                logger.error("fatal error:", exception);
                throw exception;
            }
        });
        xmlReader.parse(reader);

        return (T) vsxmlContentHandler.root;

    }

    public void writeWithEncodingHeader(Appendable writer, VSUnit unit) throws IOException {
        writer.append(encoding.header);
        writeGeneric(writer, unit);
    }

    public void writeGeneric(Appendable writer, VSUnit unit) throws IOException {
        boolean hasChildren = false;
        boolean hasValues = hasValues(unit);
        Collection<? extends VSUnit> children = ImmutableCollections.listOf();
        if (unit instanceof VSChildren) {
            VSChildren parent = F.cast(unit);
            children = parent.children();
            hasChildren = !children.isEmpty();
        }

        boolean shortTag = !hasChildren && !hasValues;

        writeTagAndAttributes(writer, unit, shortTag);

        if (!shortTag) {
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

    protected String escapeString(Object str) {
        switch (encoding) {
            case ENC1_0:
                return StringEscapeUtils.escapeXml10(String.valueOf(str));
            case ENC1_1:
                return StringEscapeUtils.escapeXml11(String.valueOf(str));
            default:
                throw new IllegalStateException("Unknown encoding:" + encoding);
        }

    }

    protected void writeValues(Appendable writer, VSUnit unit) throws IOException {
        if (unit instanceof VSTrait) {
            VSTrait traits = F.cast(unit);
            Object value = traits.traits().get(VSTraitEnum.VALUE);
            Object binary = traits.traits().get(VSTraitEnum.BINARY);

            if (value != null) {
                writer.append('<').append(VSTraitEnum.VALUE.name()).append('>');
                writer.append(escapeString(value));// non binary, so just convert to string
                writer.append('<').append('/').append(VSTraitEnum.VALUE.name()).append('>');
            }
            if (binary != null) {
                if (value != null) {
                    throw new IllegalStateException("Found value and binary in the same element, should be only one");
                }
                writer.append('<').append(VSTraitEnum.BINARY.name()).append('>');
                String encodeToString = Base64.getEncoder().encodeToString(F.cast(binary));
                writer.append(encodeToString);
                writer.append('<').append('/').append(VSTraitEnum.BINARY.name()).append('>');
            }
        }
    }

    protected void writeEndTag(Appendable writer, VSUnit unit) throws IOException {
        writer.append('<').append('/').append(escapeString(unit.getClass().getSimpleName())).append('>');
    }

    protected void writeTagAndAttributes(Appendable writer, VSUnit unit, boolean shortTag) throws IOException {
        writer.append('<').append(escapeString(unit.getClass().getSimpleName()));
        Set<Map.Entry<String, String>> entrySet = getAttributes(unit).entrySet();
        for (Map.Entry<String, String> attribute : entrySet) {
            writer.append(' ').append(attribute.getKey()).append('=')
                    .append('"').append(attribute.getValue()).append('"');
        }
        if (shortTag) {
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
