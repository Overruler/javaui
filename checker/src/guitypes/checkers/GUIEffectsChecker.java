package guitypes.checkers;

import guitypes.checkers.quals.AlwaysSafe;
import guitypes.checkers.quals.PolyUI;
import guitypes.checkers.quals.UI;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;
import checkers.quals.TypeQualifiers;
import checkers.source.Result;
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
    boolean debugSpew;

    // Even with a correct classpath, the framework doesn't seem to find the visitor class.
    @Override protected BaseTypeVisitor<?>
            createSourceVisitor(CompilationUnitTree root) {
        return new GUIEffectsVisitor(this, root);
    }

    @Override public AnnotatedTypeFactory
            createFactory(CompilationUnitTree root) {
        debugSpew = getLintOption("debugSpew", false);
        return new GUIEffectsTypeFactory(this, root, debugSpew);
    }

    public ProcessingEnvironment getEnv() {
        return getProcessingEnvironment();
    }

    @Override public void report(Result r, Object src) {
        super.report(r, src);
        if(debugSpew) {
            out(r.getMessageKeys().toString(), src);
        }
    }

    // Useful debug override; remember, the framework treats qualifiers on local variables flow-sensitively.
    //@Override
    //public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup)
    //{
    //    System.out.println("sub: " + sub + ", sup: " + sup);
    //    return super.isSubtype(sub,sup);
    //}

    public void out(String text, Object source) {
        try {
            if(isInstanceOf(source, Element.class)) {
                DiagnosticListener<? super JavaFileObject> old =
                    redirectLogListener(messager);
                findAndCall(messager, Kind.OTHER, text, source);
                setListener(messager, old);
            } else if(isInstanceOf(source, "com.sun.source.tree.Tree")) {
                DiagnosticListener<? super JavaFileObject> old =
                    redirectLogListener(trees);
                findAndCall(trees, Kind.OTHER, text, source, currentRoot);
                setListener(trees, old);
            }
            return;
        } catch(NoSuchFieldException | IllegalAccessException
            | InvocationTargetException | ClassNotFoundException
            | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        System.err.println(source + ": " + text);
    }

    private static boolean
            isInstanceOf(Object source, String name) throws ClassNotFoundException {
        return isInstanceOf(source, Class.forName(name));
    }

    private static boolean isInstanceOf(Object source, Class<?> clazz) {
        return clazz.isAssignableFrom(source.getClass());
    }

    private static void
            findAndCall(Object logger, Object... args) throws IllegalAccessException,
                    InvocationTargetException {
        Method method =
            findMethod(logger.getClass().getMethods(), args.length);
        method.invoke(logger, args);
    }

    private static Method findMethod(Method[] methods, int parameterCount) {
        for(Method method : methods) {
            if("printMessage".equals(method.getName())
                && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }
        return null;
    }

    private static DiagnosticListener<? super JavaFileObject>
            redirectLogListener(Object logger) throws NoSuchFieldException,
                    IllegalArgumentException,
                    IllegalAccessException {

        DiagnosticListener<? super JavaFileObject> old =
            getListener(logger);
        setListener(logger, new DiagnosticListener<JavaFileObject>() {
            @Override public void
                    report(Diagnostic<? extends JavaFileObject> diagnostic) {
                System.err.println(diagnostic.toString().trim());
            }
        });
        return old;
    }

    @SuppressWarnings("unchecked") private static DiagnosticListener<? super JavaFileObject>
            getListener(Object logger) throws NoSuchFieldException,
                    IllegalArgumentException,
                    IllegalAccessException {
        Field logField = getField(logger, "log");
        Object log = logField.get(logger);
        Field diagListenerField = getField(log, "diagListener");
        return (DiagnosticListener<? super JavaFileObject>) diagListenerField
            .get(log);
    }

    private static void
            setListener(Object logger,
                    DiagnosticListener<? super JavaFileObject> diagnosticListener) throws NoSuchFieldException,
                    SecurityException,
                    IllegalArgumentException,
                    IllegalAccessException {
        Field logField = getField(logger, "log");
        Object log = logField.get(logger);
        Field diagListenerField = getField(log, "diagListener");
        diagListenerField.set(log, diagnosticListener);
    }

    private static Field
            getField(Object object, String name) throws NoSuchFieldException {
        Field declaredField = object.getClass().getDeclaredField(name);
        declaredField.setAccessible(true);
        return declaredField;
    }

}
