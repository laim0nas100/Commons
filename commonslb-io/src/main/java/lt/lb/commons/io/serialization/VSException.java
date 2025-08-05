package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.Values.TypedBinaryVSU;
import java.util.Optional;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class VSException extends RuntimeException {

    public VSException(String message) {
        super(message);
    }

    public VSException(String message, Throwable cause) {
        super(message, cause);
    }

    public static enum FieldType {
        FIELD, BEAN, RECORD
    }

    public static VSException unrecognized(Class type, Optional<String> fieldName) {
        if (fieldName.isPresent()) {
            String name = fieldName.get();
            return new VSException("Unrecognized and unserializable field:" + name + " of type:" + type.getName());
        } else {
            return new VSException("Unrecognized and unserializable value of type:" + type.getName());
        }
    }

    public static VSException unrecognized(VersionedSerialization.VSUnit vsu) {
        if (vsu instanceof VersionedSerialization.TraitFieldName) {
            VersionedSerialization.TraitFieldName fn = F.cast(vsu);
            String fieldName = fn.getFieldName();
            return new VSException("Unrecognized VSUnit field:" + fieldName + " of type:" + vsu.getClass().getName());
        } else {
            return new VSException("Unrecognized VSUnit value of type:" + vsu.getClass().getName());
        }
    }

    public static VSException binaryFail(Class type, Optional<String> fieldName, Throwable cause) {
        if (fieldName.isPresent()) {
            String name = fieldName.get();
            return new VSException("Failed to serialize to binary field:" + name + " of type:" + type.getName(), cause);
        } else {
            return new VSException("Failed to serialize to binary value of type:" + type.getName(), cause);
        }
    }

    public static VSException binaryFail(TypedBinaryVSU vsu, Throwable cause) {
        if (vsu instanceof VersionedSerialization.TraitFieldName) {
            VersionedSerialization.TraitFieldName fn = F.cast(vsu);
            String fieldName = fn.getFieldName();
            return new VSException("Failed to deserialize from binary field:" + fieldName + " of type:" + vsu.getType(), cause);
        } else {
            return new VSException("Failed to deserialize from binary value of type:" + vsu.getType(), cause);
        }
    }

    public static VSException readFail(Class type, String fieldName, FieldType ft, Throwable cause) {
        switch (ft) {
            case BEAN:
                return new VSException("Failed to read property:" + fieldName + " of type:" + type.getName(), cause);
            case FIELD:
                return new VSException("Failed to read field:" + fieldName + " of type:" + type.getName(), cause);
            case RECORD:
                 return new VSException("Failed to read record component:" + fieldName + " of type:" + type.getName(), cause);
        }
        return null;
    }

    public static VSException writeFail(Class type, String fieldName, FieldType ft, Throwable cause) {
        switch (ft) {
            case BEAN:
                 return new VSException("Failed to write property:" + fieldName + " of type:" + type.getName(), cause);
            case FIELD:
                return new VSException("Failed to write field:" + fieldName + " of type:" + type.getName(), cause);
            case RECORD:
                return new VSException("Failed to write record component:" + fieldName + " of type:" + type.getName(), cause);
        }
        return null;
    }

    public static VSException fieldNotFound(Class type, String fieldName, FieldType ft) {
        switch (ft) {
            case BEAN:
                return new VSException(fieldName + " property on type " + type + " was not found");
            case FIELD:
                return new VSException(fieldName + " field on type " + type + " was not found");
            case RECORD:
                return new VSException(fieldName + " record component on type " + type + " was not found");
        }
        return null;
    }

}
