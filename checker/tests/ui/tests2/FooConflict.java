/*>>> import guitypes.checkers.quals.*;*/

public class FooConflict implements IFooSafe, IFooUI {
    //:: warning: (conflicts.inheritance)
    @Override public void foo() {}
}
