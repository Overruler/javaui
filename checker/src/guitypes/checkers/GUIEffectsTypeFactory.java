package guitypes.checkers;

import guitypes.checkers.quals.AlwaysSafe;
import guitypes.checkers.quals.PolyUI;
import guitypes.checkers.quals.PolyUIEffect;
import guitypes.checkers.quals.PolyUIType;
import guitypes.checkers.quals.SafeEffect;
import guitypes.checkers.quals.SafeType;
import guitypes.checkers.quals.UI;
import guitypes.checkers.quals.UIEffect;
import guitypes.checkers.quals.UIPackage;
import guitypes.checkers.quals.UIType;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import checkers.basetype.BaseTypeChecker;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.types.TreeAnnotator;
import checkers.util.AnnotationUtils;
import checkers.util.ElementUtils;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/**
 * Annotated type factory for the GUIEfffects Checker
 *
 * Handles determining the effect of a given method, and fixing up
 * polymorphic receiver annotations
 */
public class GUIEffectsTypeFactory
        extends
        BasicAnnotatedTypeFactory<GUIEffectsChecker> {

    protected final boolean debugSpew;

    public GUIEffectsTypeFactory(
            GUIEffectsChecker checker,
            CompilationUnitTree root,
            boolean spew) {
        // use true for flow inference
        super(checker, root, false);

        debugSpew = spew;
        postInit();
    }

    //@Override
    //protected void postDirectSuperTypes(AnnotatedTypeMirror type,
    //        List<? extends AnnotatedTypeMirror> supertypes) {
    //    // DO NOTHING!  The AnnotatedTypeFactory.postSuperTypes() removes any annotation that isn't on 'type', and doesn't do much else.
    //    if (type.getKind() == TypeKind.DECLARED) {
    //        for (AnnotatedTypeMirror supertype : supertypes) {
    //            // FIXME: Recursive initialization for defaults fields
    //            if (defaults != null) {
    //                defaults.annotate(((DeclaredType)supertype.getUnderlyingType()).asElement(), supertype);
    //            }
    //        }
    //    }
    //}

    // Could move this to a public method on the checker class
    public ExecutableElement findJavaOverride(ExecutableElement overrider,
            TypeMirror parentType) {
        if(debugSpew) {
            System.err.println("Searching overridden method for "
                + overrider + " from " + parentType);
        }
        if(parentType.getKind() != TypeKind.NONE) {
            TypeElement overriderClass =
                (TypeElement) overrider.getEnclosingElement();
            TypeElement elem =
                (TypeElement) ((DeclaredType) parentType).asElement();
            if(debugSpew) {
                System.err.println("necessary TypeElements acquired: "
                    + elem);
            }
            try {
                for(Element e : elem.getEnclosedElements()) {
                    if(debugSpew
                        && "java.lang.Object".contentEquals(elem
                            .getQualifiedName()) == false) {
                        System.err.println("Considering element " + e);
                    }
                    if(e.getKind() == ElementKind.METHOD
                        || e.getKind() == ElementKind.CONSTRUCTOR) {
                        ExecutableElement ex = (ExecutableElement) e;
                        boolean overrides =
                            checker
                                .getEnv()
                                .getElementUtils()
                                .overrides(overrider, ex, overriderClass);
                        if(overrides) {
                            if(debugSpew) {
                                System.err
                                    .println("Search overridden method: found "
                                        + ex
                                        + " for "
                                        + overrider
                                        + " from " + parentType);
                            }
                            return ex;
                        }
                    }
                }
                if(debugSpew) {
                    System.err
                        .println("Search overridden method: not found for "
                            + overrider + " from " + parentType);
                }
            } catch(Exception e) {
                System.err.println("Caught exception :" + e);
                e.printStackTrace(System.err);
                throw e;
            } catch(Error e) {
                System.err.println("Caught error :" + e);
                e.printStackTrace(System.err);
                throw e;
            }
        }
        return null;
    }

    public boolean isPolymorphicType(TypeElement cls) {
        assert cls != null;
        return getDeclAnnotation(cls, PolyUIType.class) != null
            || getDeclAnnotation(cls, PolyUI.class) != null;
    }

    public boolean isUIType(TypeElement cls) {
        if(debugSpew) {
            System.err.println(" isUIType(" + cls + ")");
        }
        AnnotationMirror targetClassUIP = getDeclAnnotation(cls, UI.class);
        AnnotationMirror targetClassUITypeP =
            getDeclAnnotation(cls, UIType.class);
        AnnotationMirror targetClassSafeTypeP =
            getDeclAnnotation(cls, SafeType.class);

        if(targetClassSafeTypeP != null) {
            if(debugSpew) {
                System.err.println("isUIType(" + cls
                    + ") explicitly marked not a UI type");
            }
            return false; // explicitly marked not a UI type
        }

        boolean hasUITypeDirectly =
            targetClassUIP != null || targetClassUITypeP != null;

        if(hasUITypeDirectly) {
            if(debugSpew) {
                System.err.println("isUIType(" + cls
                    + ") has UI Type Directly");
            }
            return true;
        }

        // anon inner classes should not inherit the package annotation, since they're so often used for closures to run async on background threads.
        if(isAnonymousType(ElementUtils.getType(cls))) {
            if(debugSpew) {
                System.err.println("isUIType(" + cls
                    + ") is Anonymous Type so no");
            }
            return false;
        }

        // We don't check polymorphic annotations so we can make a couple methods of an @UIType polymorphic explicitly
        //AnnotationMirror targetClassPolyP = getDeclAnnotation(cls, PolyUI.class);
        //AnnotationMirror targetClassPolyTypeP = getDeclAnnotation(cls, PolyUIType.class);
        AnnotationMirror targetClassSafeP =
            getDeclAnnotation(cls, AlwaysSafe.class);
        if(targetClassSafeP != null) {
            if(debugSpew) {
                System.err.println("isUIType(" + cls
                    + ") explicitly annotated AlwaysSafe");
            }
            return false; // explicitly annotated otherwise
        }

        // Look for the package
        Element packageP = cls;
        while(packageP != null
            && packageP.getKind() != ElementKind.PACKAGE) {
            packageP = packageP.getEnclosingElement();
        }
        if(packageP != null) {
            if(getDeclAnnotation(packageP, UIPackage.class) != null) {
                if(debugSpew) {
                    System.err.println("isUIType(" + cls + ") Package "
                        + packageP + " is annotated @UIPackage");
                }
                return true;
                //fromElement(classElt).addAnnotation(UIType.class);
                //System.err.println("Annotating "+classElt+" as @UIType");
            } else if(debugSpew) {
                System.err.println("isUIType(" + cls + ") Package "
                    + packageP + " found but not annotated @UIPackage");
            }
        }
        if(debugSpew) {
            System.err.println("isUIType(" + cls + ") default no");
        }

        return false;
    }

    /*
     * Calling context annotations
       To make anon-inner-classes work, I need to climb the inheritance DAG, until I:
       - find the class/interface that declares this calling method (an anon inner class is a separate class that implements an interface)
       - check whether *that* declaration specifies @UI on either the type or method
       Need to nail the the effective multiple inheritance for the effects.  Proposal:
       1. A method is UI if annotated @UI
       2. A method is UI if the enclosing class is annotated UI and the method is not annotated AlwaysSafe
       3. A method is UI if the corresponding method in the super-class/interface is UI
          and this method is not annotated @AlwaysSafe
          + A method must be *annotated* UI if the method it overrides is *annotated* UI
          + A method must be *annotated* UI if it overrides a UI method and the enclosing class is not UI
       4. It is an error if a method is UI but the same method in a super-type is not UI
       5. It is an error if two super-types specify the same method, where one type says it's UI and one says it's not
          (it's possible to simply enforce the weaker (safe) effect, but this seems more principled, it's easier ---
          backwards-compatible --- to change our minds about this later)
    */
    public Effect getDeclaredEffect(ExecutableElement methodElt) {
        if(debugSpew) {
            String text = "getDeclaredEffect " + methodElt;
            //System.err.println(text);
            out(text, methodElt);
        }
        AnnotationMirror targetUIP =
            getDeclAnnotation(methodElt, UIEffect.class);
        AnnotationMirror targetSafeP =
            getDeclAnnotation(methodElt, SafeEffect.class);
        AnnotationMirror targetPolyP =
            getDeclAnnotation(methodElt, PolyUIEffect.class);
        TypeElement targetClassElt =
            (TypeElement) methodElt.getEnclosingElement();
        if(debugSpew) {
            System.err.println("targetClassElt found " + targetClassElt);
        }

        // Short-circuit if the method is explicitly annotated
        if(targetSafeP != null) {
            if(debugSpew) {
                System.err.println("Method marked @SafeEffect");
            }
            return new Effect(SafeEffect.class);
        } else if(targetUIP != null) {
            if(debugSpew) {
                System.err.println("Method marked @UIEffect");
            }
            return new Effect(UIEffect.class);
        } else if(targetPolyP != null) {
            if(debugSpew) {
                System.err.println("Method marked @PolyUIEffect");
            }
            return new Effect(PolyUIEffect.class);
        }

        if(isUIType(targetClassElt)) {
            if(debugSpew) {
                System.err
                    .println("Method marked @UIEffect based on class");
            }
            return new Effect(UIEffect.class);
        }

        // Anonymous inner types should probably just get the effect of the parent by default, rather than
        // annotating every instance.  Unless it's implementing a polymorphic supertype, in which case we
        // still want the developer to be explicit.
        if(isAnonymousType(ElementUtils.getType(targetClassElt))) {
            //System.err.println("Considering "+targetClassElt+"."+methodElt);
            boolean canInheritParentEffects = true; // Refine this for polymorphic parents
            DeclaredType directSuper =
                (DeclaredType) targetClassElt.getSuperclass();
            TypeElement superElt = (TypeElement) directSuper.asElement();
            // Anonymous subtypes of polymorphic classes other than object can't inherit
            if(getDeclAnnotation(superElt, PolyUIType.class) != null
                && !superElt.getQualifiedName().contentEquals(
                    "java.lang.Object")) {
                canInheritParentEffects = false;
                //System.err.println("supertype "+superElt+" is polymorphic but not Object");
            } else {
                for(TypeMirror ifaceM : targetClassElt.getInterfaces()) {
                    DeclaredType iface = (DeclaredType) ifaceM;
                    TypeElement ifaceElt = (TypeElement) iface.asElement();
                    if(getDeclAnnotation(ifaceElt, PolyUIType.class) != null) {
                        canInheritParentEffects = false;
                        //System.err.println("superinterface "+ifaceElt+" is polymorphic");
                    }
                }
            }

            if(canInheritParentEffects) {
                //System.err.println(targetClassElt+"."+methodElt+" can inherit the parent effect.");
                Effect.EffectRange r =
                    findInheritedEffectRange(targetClassElt, methodElt);
                return r != null ? Effect.min(r.min, r.max) : new Effect(
                    SafeEffect.class);
            }
        }

        return new Effect(SafeEffect.class);
    }

    public boolean hasAnnotationByName(AnnotatedTypeMirror m, Class<?> c) {
        for(AnnotationMirror a : m.getAnnotations()) {
            Name cname =
                checker
                    .getEnv()
                    .getElementUtils()
                    .getName(c.getCanonicalName());
            Name annoName = AnnotationUtils.annotationName(a);
            // Use contentEquals here to work around the weird behavior that AnnotatedTypeMirror.hasAnnotation() has with comparing Names by .equals.
            if(cname.contentEquals(annoName)) {
                return true;
            }
        }
        return false;
    }

    // Only the visitMethod call should pass true for warnings
    public Effect.EffectRange
            findInheritedEffectRange(TypeElement declaringType,
                    ExecutableElement overridingMethod) {
        return findInheritedEffectRange(declaringType, overridingMethod,
            false, null);
    }

    public Effect.EffectRange
            findInheritedEffectRange(TypeElement declaringType,
                    ExecutableElement overridingMethod,
                    boolean issueConflictWarning,
                    Tree errorNode) {
        assert declaringType != null;
        ExecutableElement ui_override = null;
        ExecutableElement safe_override = null;
        ExecutableElement poly_override = null;
        // We must account for explicit annotation, type declaration annotations, and package annotations
        boolean isUI =
            (getDeclAnnotation(overridingMethod, UIEffect.class) != null || isUIType(declaringType))
                //getDeclAnnotation(declaringType, UIType.class) != null ||
                //getDeclAnnotation(declaringType, UI.class) != null ||
                //(getDeclAnnotation(ElementUtils.enclosingPackage(declaringType), UIPackage.class) != null
                // && !isAnonymousType(ElementUtils.getType(declaringType))))
                && getDeclAnnotation(overridingMethod, SafeEffect.class) == null;
        boolean isPolyUI =
            getDeclAnnotation(overridingMethod, PolyUIEffect.class) != null;
        // TODO: We must account for @UI and @AlwaysSafe annotations for extends and implements clauses, and do the proper
        //       substitution of @Poly effects and quals!
        @SuppressWarnings("unused") List<? extends TypeMirror> interfaces =
            declaringType.getInterfaces();
        TypeMirror superclass = declaringType.getSuperclass();
        while(superclass != null && superclass.getKind() != TypeKind.NONE) {
            ExecutableElement overrides =
                findJavaOverride(overridingMethod, superclass);
            if(overrides != null) {
                Effect eff = getDeclaredEffect(overrides);
                assert eff != null;
                if(eff.isSafe()) {
                    // found a safe override
                    safe_override = overrides;
                    if(isUI && issueConflictWarning) {
                        checker.report(Result.failure(
                            "conflicts.override", superclass + "."
                                + safe_override), errorNode);
                    }
                    if(isPolyUI && issueConflictWarning) {
                        checker.report(Result.failure(
                            "conflicts.override.polymorphic", superclass
                                + "." + safe_override), errorNode);
                    }
                } else if(eff.isUI()) {
                    // found a ui override
                    ui_override = overrides;
                } else {
                    assert eff.isPoly();
                    poly_override = overrides;
                    // TODO: Is this right? is the supertype covered by the directSuperTypes() method all I need?  Or should I be using that utility method that returns a set of annodecl-method pairs given a method that overrides stuff
                    //if (isUI && issueConflictWarning) {
                    //    AnnotatedTypeMirror.AnnotatedDeclaredType supdecl = fromElement((TypeElement)(((DeclaredType)superclass).asElement()));//((DeclaredType)superclass).asElement());
                    //    // Need to special case an anonymous class with @UI on the decl, because "new @UI Runnable {...}" parses as @UI on an anon class decl extending Runnable
                    //    boolean isAnonInstantiation = isAnonymousType(ElementUtils.getType(declaringType)) && getDeclAnnotation(declaringType, UI.class) != null;
                    //    if (!isAnonInstantiation && !hasAnnotationByName(supdecl, UI.class)) {
                    //        checker.report(Result.failure("conflicts.override", "non-UI instantiation of "+supdecl), errorNode);
                    //    }
                    //}
                }
            }
            DeclaredType decl = (DeclaredType) superclass;
            superclass = ((TypeElement) decl.asElement()).getSuperclass();
        }
        AnnotatedTypeMirror.AnnotatedDeclaredType annoDecl =
            fromElement(declaringType);
        //for (TypeMirror ty : interfaces) {
        for(AnnotatedTypeMirror.AnnotatedDeclaredType ty : annoDecl
            .directSuperTypes()) {
            //ExecutableElement overrides = findJavaOverride(overridingMethod, ty);
            ExecutableElement overrides =
                findJavaOverride(overridingMethod, ty.getUnderlyingType());
            if(overrides != null) {
                Effect eff = getDeclaredEffect(overrides);
                if(eff.isSafe()) {
                    // found a safe override
                    safe_override = overrides;
                    if(isUI && issueConflictWarning) {
                        checker.report(
                            Result.failure("conflicts.override", ty + "."
                                + safe_override), errorNode);
                    }
                    if(isPolyUI && issueConflictWarning) {
                        checker.report(Result.failure(
                            "conflicts.override.polymorphic", ty + "."
                                + safe_override), errorNode);
                    }
                } else if(eff.isUI()) {
                    // found a ui override
                    ui_override = overrides;
                } else {
                    assert eff.isPoly();
                    poly_override = overrides;
                    if(isUI && issueConflictWarning) {
                        //AnnotatedTypeMirror.AnnotatedDeclaredType supdecl = fromElement((TypeElement)(((DeclaredType)ty).asElement()));//((DeclaredType)superclass).asElement());
                        AnnotatedTypeMirror.AnnotatedDeclaredType supdecl =
                            ty;
                        // Need to special case an anonymous class with @UI on the decl, because "new @UI Runnable {...}" parses as @UI on an anon class decl extending Runnable
                        boolean isAnonInstantiation =
                            isAnonymousType(ElementUtils
                                .getType(declaringType))
                                && getDeclAnnotation(declaringType,
                                    UI.class) != null;
                        if(!isAnonInstantiation
                            && !hasAnnotationByName(supdecl, UI.class)) {
                            checker.report(Result.failure(
                                "conflicts.override",
                                "non-UI instantiation of " + supdecl),
                                errorNode);
                        }
                    }
                }
            }
        }

        // We don't need to issue warnings for inheriting from poly and a concrete effect.
        if(ui_override != null && safe_override != null
            && issueConflictWarning) {
            // There may be more than two parent methods, but for now it's enough to know there are at least 2 in conflict
            checker.report(
                Result.warning("conflicts.inheritance", ui_override
                    .getEnclosingElement()
                    .asType()
                    .toString()
                    + "." + ui_override.toString(), safe_override
                    .getEnclosingElement()
                    .asType()
                    .toString()
                    + "." + safe_override.toString()), errorNode);
        }

        Effect min =
            safe_override != null ? new Effect(SafeEffect.class)
                : poly_override != null ? new Effect(PolyUIEffect.class)
                    : ui_override != null ? new Effect(UIEffect.class)
                        : null;
        Effect max =
            ui_override != null ? new Effect(UIEffect.class)
                : poly_override != null ? new Effect(PolyUIEffect.class)
                    : safe_override != null ? new Effect(SafeEffect.class)
                        : null;
        if(debugSpew) {
            System.err.println("Found " + declaringType + "."
                + overridingMethod + " to have inheritance pair (" + min
                + "," + max + ")");
        }
        if(min == null && max == null) {
            return null;
        }
        return new Effect.EffectRange(min, max);
    }

    private static boolean isAnonymousType(TypeMirror type) {
        TypeElement element =
            (TypeElement) ((DeclaredType) type).asElement();
        Name qualifiedName = element.getQualifiedName();
        return "".contentEquals(qualifiedName);
    }

    @Override protected TreeAnnotator
            createTreeAnnotator(GUIEffectsChecker checker2) {
        return new GUIEffectsTreeAnnotator(checker2);
    }

    void out(String text, Object node) {
        checker.out(text, node);
    }

    /**
     * A class for adding annotations based on tree
     */
    private class GUIEffectsTreeAnnotator extends TreeAnnotator {
        GUIEffectsTreeAnnotator(BaseTypeChecker checker) {
            super(checker, GUIEffectsTypeFactory.this);
        }

        public boolean hasExplicitUIEffect(ExecutableElement methElt) {
            return getDeclAnnotation(methElt, UIEffect.class) != null;
        }

        public boolean hasExplicitSafeEffect(ExecutableElement methElt) {
            return getDeclAnnotation(methElt, SafeEffect.class) != null;
        }

        public boolean hasExplicitPolyUIEffect(ExecutableElement methElt) {
            return getDeclAnnotation(methElt, PolyUIEffect.class) != null;
        }

        public boolean hasExplicitEffect(ExecutableElement methElt) {
            return hasExplicitUIEffect(methElt)
                || hasExplicitSafeEffect(methElt)
                || hasExplicitPolyUIEffect(methElt);
        }

        @Override public Void visitMethod(MethodTree node,
                AnnotatedTypeMirror type) {
            AnnotatedTypeMirror.AnnotatedExecutableType methType =
                (AnnotatedTypeMirror.AnnotatedExecutableType) type;
            Effect e = getDeclaredEffect(methType.getElement());
            TypeElement cls =
                (TypeElement) methType.getElement().getEnclosingElement();
            // STEP 1: Get the method effect annotation
            if(!hasExplicitEffect(methType.getElement())) {
                if(debugSpew) {
                    System.err.println("No explicit effect annotation on "
                        + node.getName());
                    System.err.println("Could want to be annotating with "
                        + e);
                }
                // TODO: This line does nothing!  AnnotatedTypeMirror.addAnnotation silently ignores non-qualifier annotations!
                // We should be digging up the /declaration/ of the method, and annotating that
                methType.addAnnotation(e.getAnnot());
            }
            // STEP 2: Fix up the method receiver annotation
            AnnotatedTypeMirror.AnnotatedDeclaredType receiverType =
                methType.getReceiverType();
            if(receiverType.getAnnotations().isEmpty()) {
                if(debugSpew) {
                    System.err.println("Fixing annotations in "
                        + node.getName());
                    System.err.println(type.getKind() + ":" + type);
                }
                AnnotatedTypeMirror receiver = receiverType;//methType.getReceiverType();
                if(debugSpew) {
                    System.err.println("Receiver type kind: "
                        + receiver.getKind());
                }
                receiver.clearAnnotations();
                receiver.addAnnotation(isPolymorphicType(cls)
                    ? PolyUI.class
                    : getDeclAnnotation(cls, UI.class) != null ? UI.class
                        : AlwaysSafe.class);
                if(debugSpew) {
                    System.err
                        .println("Cleared and adding appropriate annot to receiver:"
                            + type);
                }
            }
            return super.visitMethod(node, type);
        }

        // TODO: Delete this method and test that behavior (and results!) are preserved
        @Override public Void visitVariable(VariableTree node,
                AnnotatedTypeMirror type) {
            //if (node.getName().contentEquals("this")) {
            //    //MethodTree m = TreeUtils.enclosingMethod(trees.getPath(TreeUtils.elementFromUse(node)));
            //    //System.err.println("Found instance of this in "+m.getName());
            //    //if (m.getReceiverAnnotations().isEmpty()) {
            //    //    ExecutableElement e = TreeUtils.elementFromDeclaration(m);
            //    //    // Need access to the visitor here, but there's no way to get to the visitor from the checker...
            //    //    Effect eff = visitor.getDeclaredEffect(e);
            //    //    Tree t = node.getType();
            //    //    // TODO: Set the annotation on the type based on the method's effect
            //    //    System.err.println("this:"+type);
            //    //}
            //}
            return super.visitVariable(node, type);
        }
    }
}
