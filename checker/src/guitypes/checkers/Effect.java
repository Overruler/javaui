package guitypes.checkers;

import guitypes.checkers.quals.PolyUIEffect;
import guitypes.checkers.quals.SafeEffect;
import guitypes.checkers.quals.UIEffect;

import java.lang.annotation.Annotation;

public final class Effect {
    // I hate Java's comparable interface, so I'm not using it

    private final Class<? extends Annotation> annotClass;

    public Effect(Class<? extends Annotation> cls) {
        assert cls.equals(UIEffect.class)
            || cls.equals(PolyUIEffect.class)
            || cls.equals(SafeEffect.class);
        annotClass = cls;
    }

    public static boolean LE(Effect left, Effect right) {
        assert left != null && right != null;
        boolean leftBottom = left.annotClass.equals(SafeEffect.class);
        boolean rightTop = right.annotClass.equals(UIEffect.class);
        return leftBottom || rightTop
            || left.annotClass.equals(right.annotClass);
    }

    public static Effect min(Effect l, Effect r) {
        return LE(l, r) ? l : r;
    }

    public static final class EffectRange {
        public final Effect min, max;

        public EffectRange(Effect min, Effect max) {
            assert min != null || max != null;
            // If one is null, fill in with the other
            this.min = min != null ? min : max;
            this.max = max != null ? max : min;
        }
    }

    public boolean isSafe() {
        return annotClass.equals(SafeEffect.class);
    }

    public boolean isUI() {
        return annotClass.equals(UIEffect.class);
    }

    public boolean isPoly() {
        return annotClass.equals(PolyUIEffect.class);
    }

    public Class<? extends Annotation> getAnnot() {
        return annotClass;
    }

    @Override public String toString() {
        return annotClass.getSimpleName();
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
            prime * result
                + (annotClass == null ? 0 : annotClass.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        Effect other = (Effect) obj;
        if(annotClass == null) {
            if(other.annotClass != null) {
                return false;
            }
        } else if(!annotClass.equals(other.annotClass)) {
            return false;
        }
        return true;
    }
}
