package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

/**
 * Annotation for the UI effect
 */
@Documented
// likely unnecessary
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE_USE, ElementType.TYPE_PARAMETER,
    //ElementType.TYPE,
    //ElementType.METHOD,
    //ElementType.FIELD,
    //ElementType.CONSTRUCTOR,
    ElementType.PARAMETER, ElementType.LOCAL_VARIABLE
})
@SubtypeOf({})
@TypeQualifier
public @interface UI {}
