package guitypes.checkers;

import guitypes.checkers.quals.AlwaysSafe;
import guitypes.checkers.quals.PolyUI;
import guitypes.checkers.quals.UI;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;
import checkers.quals.TypeQualifiers;
import checkers.source.SupportedLintOptions;
import checkers.types.AnnotatedTypeFactory;

import com.sun.source.tree.CompilationUnitTree;

/*
 * Do NOT claim UIType, or mark UIType @TypeQualifier.  Otherwise the checker framework interprets
 * its presence on a class declaration in some weird way that causes errors on method calls
 * to @AlwaysSafe instances of the annotated class.
 */
@SupportedLintOptions({
    "debugSpew"
})
@TypeQualifiers({
    // Actual qualifiers
    UI.class, PolyUI.class, AlwaysSafe.class
// Annotations we need to declare to use through the annotated type mirror framework
//UIEffect.class, SafeEffect.class, PolyUIEffect.class, PolyUIType.class, UIType.class
})
public class GUIEffectsChecker extends BaseTypeChecker {

    // Even with a correct classpath, the framework doesn't seem to find the visitor class.
    @Override protected BaseTypeVisitor<?>
            createSourceVisitor(CompilationUnitTree root) {
        return new GUIEffectsVisitor(this, root);
    }

    @Override public AnnotatedTypeFactory
            createFactory(CompilationUnitTree root) {
        return new GUIEffectsTypeFactory(this, root, getLintOption(
            "debugSpew", false));
    }

    public ProcessingEnvironment getEnv() {
        return getProcessingEnvironment();
    }

    // Useful debug override; remember, the framework treats qualifiers on local variables flow-sensitively.
    //@Override
    //public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup)
    //{
    //    System.out.println("sub: " + sub + ", sup: " + sup);
    //    return super.isSubtype(sub,sup);
    //}

}
