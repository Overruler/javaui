package guitypes.checkers.quals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import checkers.quals.SubtypeOf;

/**
 * Class declaration annotation to make methods default to @UI
 *
 * Do NOT mark this @TypeQualifier. It is not necessary to get access
 * to it, and doing so directs the checker framework to add all sorts
 * of extra weird semantics to its use.
 */
@Documented
// likely unnecessary
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE
})
// Need to specify this so the annotated type mirror framework will not choke on this
@SubtypeOf({})
public @interface UIType {}
