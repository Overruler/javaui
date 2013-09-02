/*>>> import guitypes.checkers.quals.*;*/

@SuppressWarnings({
    "static-method", "unused"
})
public class AssignmentTests {
    public static/*@PolyUIType*/class P {}

    // We must separate these tests, otherwise the flow sensitivity kicks in and confounds the test results
    public void testBody1(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@UI*/P ui2 = safe;
    }

    public void testBody2(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@UI*/P ui2 = ui;
    }

    public void testBody3(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@UI*/P ui2 = poly;
    }

    public void testBody4(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@AlwaysSafe*/P safe2 = safe;
    }

    public void testBody5(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        //:: error: (assignment.type.incompatible)
        /*@AlwaysSafe*/P safe2 = ui;
    }

    public void testBody6(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        //:: error: (assignment.type.incompatible)
        /*@AlwaysSafe*/P safe2 = poly;
    }

    public void testBody7(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@PolyUI*/P poly2 = safe;
    }

    public void testBody8(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        /*@PolyUI*/P poly2 = poly;
    }

    public void testBody9(/*@UI*/P ui, /*@AlwaysSafe*/
            P safe, /*@PolyUI*/
            P poly) {
        //:: error: (assignment.type.incompatible)
        /*@PolyUI*/P poly2 = ui;
    }
}
