package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.PolymorphicQualifier;
import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

/**
 * Annotation for the polymorphic-UI effect
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
@SubtypeOf({
    UI.class
})
@TypeQualifier
// We want this so we can use this as the default receiver annotation in @PolyUIEffect method bodies
// This way the framework will automatically restrict other @ClassPolyUI arguments to a polymorphic
// method to the same version as the receiver.
@PolymorphicQualifier
public @interface ClassPolyUI {}
