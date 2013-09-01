package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.SubtypeOf;

/**
 * Annotation for the polymorphic type declaration
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE
})
// This is an annotation to declare a type as taking an effect parameter
// For extensions, we'll need to do @PolyUIType class Subclass extends @PolyUI Superclass
// Need to specify this so the annotated type mirror framework will not choke on this
@SubtypeOf({})
public @interface PolyUIType {}
