package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

/**
 * Annotation to override the UI effect on a class, and make a field
 * or method safe for non-UI code to use.
 */
@Documented
// likely unnecessary
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE_USE, ElementType.TYPE_PARAMETER,
    //ElementType.METHOD,
    //ElementType.FIELD,
    //ElementType.CONSTRUCTOR,
    ElementType.PARAMETER, ElementType.LOCAL_VARIABLE
})
// If I restrict type shape appropriately, and make the default receiver
// annotation for polyclasses PolyUI, I don't need ClassPolyUI
//@SubtypeOf({PolyUI.class,ClassPolyUI.class})
@SubtypeOf({
    AlwaysSafe.class
})
//@DefaultQualifierInHierarchy
//@ImplicitFor(trees={Tree.Kind.NULL_LITERAL})
@TypeQualifier
public @interface Safe {}
