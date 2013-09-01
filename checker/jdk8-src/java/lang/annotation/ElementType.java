package java.lang.annotation;

/*
 * Included to allow Java 7 compiled annotations to work properly in Checker Framework by
 * using @Target(TYPE_PARAMETER, TYPE_USE) meta-annotations where appropriate. The semantic
 * meaning is that of Checker Framework, not Java 8. This enum is not included in the binary
 * distribution jar-files.
 */
public enum ElementType {
    TYPE,
    FIELD,
    METHOD,
    PARAMETER,
    CONSTRUCTOR,
    LOCAL_VARIABLE,
    ANNOTATION_TYPE,
    PACKAGE,
    TYPE_PARAMETER,
    TYPE_USE
}
