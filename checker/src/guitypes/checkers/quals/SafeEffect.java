package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.SubtypeOf;

/**
 * Annotation for the concrete safe effect on methods, or on field
 * accesses
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD
})
// This is NOT a type qualifier: it is an effect
// Need to specify this so the annotated type mirror framework will not choke on this
@SubtypeOf({})
public @interface SafeEffect {}
