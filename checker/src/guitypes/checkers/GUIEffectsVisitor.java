package guitypes.checkers;

import guitypes.checkers.quals.AlwaysSafe;
import guitypes.checkers.quals.PolyUI;
import guitypes.checkers.quals.PolyUIEffect;
import guitypes.checkers.quals.SafeEffect;
import guitypes.checkers.quals.UI;
import guitypes.checkers.quals.UIEffect;

import java.util.Stack;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.TreeUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * Require that only UI code invokes code with the UI effect.
 */
public class GUIEffectsVisitor extends BaseTypeVisitor<GUIEffectsChecker> {
    // To count hits for debugger breakpoints.
    public static int hitCounter = 0;
    protected final boolean debugSpew;

    // effStack and currentMethods should always be the same size.
    protected final Stack<Effect> effStack;
    protected final Stack<MethodTree> currentMethods;
    protected final GUIEffectsTypeFactory getypeFactory;

    public GUIEffectsVisitor(
            GUIEffectsChecker checker,
            CompilationUnitTree root) {
        super(checker, root);
        debugSpew = checker.getLintOption("debugSpew", false);
        if(debugSpew) {
            System.err.println("Running GUIEffectsVisitor");
        }
        effStack = new Stack<>();
        currentMethods = new Stack<>();
        getypeFactory = (GUIEffectsTypeFactory) atypeFactory;
    }

    // This is a framework method that checks receiver annotations on invocation.
    // The issue is that the receiver implicitly receives an @AlwaysSafe anno, so calls on @UI
    // references fail because the framework doesn't implicitly upcast the receiver (which in
    // general wouldn't be sound).
    // TODO: Fix method receiver defaults: see comment below
    @Override protected void
            checkMethodInvocability(AnnotatedExecutableType method,
                    MethodInvocationTree node) {
        // The inherited version of this complains about invoking methods on @UI instantiations of
        // classes, which by default are annotated with @AlwaysSafe receivers.  But we're allowing
        // this, for now.  (The real invocation check is performed in visitMethodInvocation().)
        // Perhaps the right fix is to simply have the type factory make all implicit receiver
        // annotations @PolyUI - it would work right for methods with explicit effects, including
        // for safe methods of a class with polymorphic methods...
    }

    @Override protected boolean checkOverride(MethodTree overriderTree,
            AnnotatedTypeMirror.AnnotatedDeclaredType enclosingType,
            AnnotatedTypeMirror.AnnotatedExecutableType overridden,
            AnnotatedTypeMirror.AnnotatedDeclaredType overriddenType,
            Void p) {
        // Method override validity is checked manually by the type factory during visitation
        return true;
    }

    @Override public boolean
            isValidUse(AnnotatedTypeMirror.AnnotatedDeclaredType declarationType,
                    AnnotatedTypeMirror.AnnotatedDeclaredType useType) {
        boolean ret =
            useType.hasAnnotation(AlwaysSafe.class)
                || getypeFactory
                    .isPolymorphicType((TypeElement) declarationType
                        .getElement()) || useType.hasAnnotation(UI.class)
                && declarationType.hasAnnotation(UI.class);
        if(!ret) {
            System.err.println("use: " + useType);
            System.err.println("use safe: "
                + useType.hasAnnotation(AlwaysSafe.class));
            System.err.println("use poly: "
                + useType.hasAnnotation(PolyUI.class));
            System.err.println("use ui: "
                + useType.hasAnnotation(UI.class));
            System.err.println("declaration safe: "
                + declarationType.hasAnnotation(AlwaysSafe.class));
            System.err.println("declaration poly: "
                + getypeFactory
                    .isPolymorphicType((TypeElement) declarationType
                        .getElement()));
            System.err.println("declaration ui: "
                + declarationType.hasAnnotation(UI.class));
            System.err.println("declaration: " + declarationType);
        }
        return ret;
    }

    // Check that the invoked effect is <= permitted effect (effStack.peek())
    @Override public Void visitMethodInvocation(MethodInvocationTree node,
            Void p) {
        if(debugSpew) {
            String text =
                "**** Visit invocation of method " + node + " in "
                    + currentMethods.peek().getName();
            System.err.println(text);
            out(text, node);
        }
        // Target method annotations
        ExecutableElement methodElt = TreeUtils.elementFromUse(node);

        MethodTree callerTree =
            TreeUtils.enclosingMethod(getCurrentPath());
        if(callerTree == null) {
            // Static initializer; let's assume this is safe to have the UI effect
            if(debugSpew) {
                System.err
                    .println("No enclosing method: likely static initializer");
            }
            return super.visitMethodInvocation(node, p);
        }

        ExecutableElement callerElt =
            TreeUtils.elementFromDeclaration(callerTree);
        if(debugSpew) {
            System.err.println("caller found, tree: " + callerTree
                + ", element: " + callerElt);
        }

        Effect targetEffect = getypeFactory.getDeclaredEffect(methodElt);
        //System.err.println("Dispatching method "+node+"on "+node.getMethodSelect());
        if(targetEffect.isPoly()) {
            AnnotatedTypeMirror srcType = null;
            assert node.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER
                || node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT;
            if(node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                ExpressionTree src =
                    ((MemberSelectTree) node.getMethodSelect())
                        .getExpression();
                srcType = atypeFactory.fromExpression(src);
            } else {
                // Tree.Kind.IDENTIFIER, e.g. a direct call like "super()"
                srcType = visitorState.getMethodReceiver();
            }

            // Instantiate type-polymorphic effects
            if(getypeFactory
                .hasAnnotationByName(srcType, AlwaysSafe.class)/*srcType.hasAnnotation(AlwaysSafe.class)*/) {
                //System.err.println("Instantiating effect as Safe");
                targetEffect = new Effect(SafeEffect.class);
            } else if(getypeFactory.hasAnnotationByName(srcType, UI.class) /*srcType.hasAnnotation(UI.class)*/) {
                //System.err.println("Instantiating effect as UI");
                targetEffect = new Effect(UIEffect.class);
            }
            // Poly substitution would be a noop.
        }
        Effect callerEffect = getypeFactory.getDeclaredEffect(callerElt);
        assert callerEffect.equals(effStack.peek());

        if(!Effect.LE(targetEffect, callerEffect)) {
            checker.report(Result.failure("call.invalid.ui", targetEffect,
                callerEffect), node);
            if(debugSpew) {
                out("Issuing error for node", node);
            }
        } else {
            if(debugSpew) {
                out("Not Issuing error for node", node);
            }
        }
        if(debugSpew) {
            System.err
                .println("Successfully finished main non-recursive checking of invocation "
                    + node);
        }

        return super.visitMethodInvocation(node, p);
    }

    @Override public Void visitMethod(MethodTree node, Void p) {
        // TODO: If the type we're in is a polymorphic (over effect qualifiers) type, the receiver must be @ClassPolyUI (or upon further thought, @PolyUI so it's bound at call sites)
        //       Otherwise a "non-polymorphic" method of a polymorphic type could be called on a UI instance, which then
        //       gets a Safe reference to itself (unsound!) that it can then pass off elsewhere (dangerous!).  So all
        //       receivers in methods of a @PolyUIType must be @ClassPolyUI.
        // TODO: What do we do then about classes that inherit from a concrete instantiation?  If it subclasses a Safe
        //       instantiation, all is well.  If it subclasses a UI instantiation, then the receivers should probably
        //       be @UI in both new and override methods, so calls to polymorphic methods of the parent class will work
        //       correctly.  In which case for proving anything, the qualifier on sublasses of UI instantiations would
        //       always have to be @UI... Need to write down |- t for this system!  And the judgments for method overrides
        //       and inheritance!  Those are actually the hardest part of the system.

        ExecutableElement methElt = TreeUtils.elementFromDeclaration(node);
        if(debugSpew) {
            System.err.println("\nVisiting method " + methElt);
        }

        // Check for conflicting (multiple) annotations
        assert methElt != null;
        @SuppressWarnings("unused") TypeMirror scratch =
            methElt.getReturnType();
        AnnotationMirror targetUIP =
            atypeFactory.getDeclAnnotation(methElt, UIEffect.class);
        AnnotationMirror targetSafeP =
            atypeFactory.getDeclAnnotation(methElt, SafeEffect.class);
        AnnotationMirror targetPolyP =
            atypeFactory.getDeclAnnotation(methElt, PolyUIEffect.class);
        TypeElement targetClassElt =
            (TypeElement) methElt.getEnclosingElement();
        if(targetUIP != null
            && (targetSafeP != null || targetPolyP != null)
            || targetSafeP != null && targetPolyP != null) {
            checker.report(Result.failure("conflicts.annotations"), node);
        }
        if(targetPolyP != null
            && !getypeFactory.isPolymorphicType(targetClassElt)) {
            checker.report(Result.failure("polymorphism.invalid"), node);
        }
        if(targetUIP != null && getypeFactory.isUIType(targetClassElt)) {
            checker.report(Result.warning("effects.redundant.uitype"),
                node);
        }

        // TODO: Report an error for polymorphic method bodies??? Until we fix the receiver defaults, it won't really be correct
        @SuppressWarnings("unused") Effect.EffectRange range =
            getypeFactory.findInheritedEffectRange(
                (TypeElement) methElt.getEnclosingElement(), methElt,
                true, node);
        if(targetUIP == null && targetSafeP == null && targetPolyP == null) {
            // implicitly annotate this method with the LUB of the effects of the methods it overrides
            //atypeFactory.fromElement(methElt).addAnnotation(range != null ? range.min.getAnnot() : (isUIType(((TypeElement)methElt.getEnclosingElement())) ? UI.class : AlwaysSafe.class));
            // TODO: This line does nothing!  AnnotatedTypeMirror.addAnnotation silently ignores non-qualifier annotations!
            //System.err.println("ERROR: TREE ANNOTATOR SHOULD HAVE ADDED EXPLICIT ANNOTATION! ("+node.getName()+")");
            atypeFactory.fromElement(methElt).addAnnotation(
                getypeFactory.getDeclaredEffect(methElt).getAnnot());
        }

        // We hang onto the current method here for ease.  We back up the old
        // current method because this code is reentrant when we traverse methods of an inner class
        currentMethods.push(node);
        //effStack.push(targetSafeP != null ? new Effect(AlwaysSafe.class) :
        //                (targetPolyP != null ? new Effect(PolyUI.class) :
        //                   (targetUIP != null ? new Effect(UI.class) :
        //                      (range != null ? range.min : (isUIType(((TypeElement)methElt.getEnclosingElement())) ? new Effect(UI.class) : new Effect(AlwaysSafe.class))))));
        effStack.push(getypeFactory.getDeclaredEffect(methElt));
        if(debugSpew) {
            System.err.println("Pushing " + effStack.peek()
                + " onto the stack when checking " + methElt);
        }

        Void ret = super.visitMethod(node, p);
        if(debugSpew) {
            System.err.println("Popping " + effStack.peek()
                + " from the stack when checking " + methElt);
        }
        effStack.pop();
        currentMethods.pop();
        return ret;
    }

    @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
        //TODO: Same effect checks as for methods
        return super.visitMemberSelect(node, p);
    }

    @Override public Void visitClass(ClassTree node, Void p) {
        // TODO: Check constraints on this class decl vs. parent class decl., and interfaces
        // TODO: This has to wait for now: maybe this will be easier with the isValidUse on the TypeFactory
        @SuppressWarnings("unused") AnnotatedTypeMirror.AnnotatedDeclaredType atype =
            atypeFactory.fromClass(node);
        //System.err.print("Visiting "+atype+" <: ");
        //for (AnnotatedTypeMirror.AnnotatedDeclaredType sup : atype.directSuperTypes()) {
        //    System.err.print(" | "+sup);
        //}
        //System.err.println();
        //if (node.getExtendsClause()!= null)
        //    System.err.println("     has extends "+node.getExtendsClause()+"("+node.getExtendsClause().getKind()+")");
        //for (Tree impl : node.getImplementsClause()) {
        //    System.err.println("    and implements "+impl+"("+impl.getKind()+")");
        //}
        //for (AnnotatedTypeMirror.AnnotatedDeclaredType parent : atype.directSuperTypes()) {
        //    System.err.println(atype+"("+atype.getElement()+")"+"<:"+parent+"("+parent.getElement()+")");
        //    if (getypeFactory.isPolymorphicType((TypeElement)atype.getElement()) && !getypeFactory.isPolymorphicType((TypeElement)parent.getElement())) {
        //        // TODO: Think hard about this: Shouldn't a polymorphic type be allowed to implement a
        //        // monomorphic interface???
        //        checker.report(Result.failure("inheritance.polymorphic.invalid", atype, parent), node);
        //    }
        //}

        //if (debugSpew) {
        //    System.err.println("Visiting class "+TreeUtils.elementFromDeclaration(node));
        //    AnnotatedTypeMirror.AnnotatedDeclaredType mirror = atypeFactory.fromClass(node);
        //    for (AnnotatedTypeMirror.AnnotatedDeclaredType d : mirror.directSuperTypes()) { // There's also a directSuperTypesField()??
        //        System.err.print(d+" ");
        //    }
        //    System.err.println();
        //}

        // Push a null method and UI effect onto the stack for static field initialization
        // TODO: Figure out if this is safe!  For static data, almost certainly, but for statically initialized instance fields, I'm assuming those are implicitly moved into each constructor, which must then be @UI
        currentMethods.push(null);
        effStack.push(new Effect(UIEffect.class));
        Void ret = super.visitClass(node, p);
        currentMethods.pop();
        effStack.pop();
        return ret;
    }

    void out(String text, Object node) {
        checker.out(text, node);
    }
}
