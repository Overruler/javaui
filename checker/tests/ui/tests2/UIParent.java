/*>>> import guitypes.checkers.quals.*;*/

/*@UIType*/
public class UIParent {
    protected UIElement thingy;

    /*@SafeEffect*/public UIParent() {} // Making this ctor safe to allow easy safe subclasses

    public void doingUIStuff() {
        // should have UI effect
        thingy.dangerous();
    }

    /*@SafeEffect*/
    public void doingSafeStuff() {} // non-UI

}
