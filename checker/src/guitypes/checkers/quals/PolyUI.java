package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.PolymorphicQualifier;
import checkers.quals.TypeQualifier;

/**
 * Annotation for the polymorphic-UI effect
 */
@Documented
// likely unnecessary
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.TYPE,
    //ElementType.METHOD,
    // IT IS A BUG to allow ElementType.FIELD!  In this case, subtyping on 'this'
    // can effectively change the field instantiation, introducing unsoundness!
    // ElementType.FIELD,
    //ElementType.CONSTRUCTOR,
    ElementType.PARAMETER, ElementType.LOCAL_VARIABLE
})
//@SubtypeOf({UI.class})
@PolymorphicQualifier
@TypeQualifier
public @interface PolyUI {}
