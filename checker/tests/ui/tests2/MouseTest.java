import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*>>> import guitypes.checkers.quals.*;*/

/*@UIType*/
class MouseTest extends MouseAdapter {
    // Test the stub file handling
    @SuppressWarnings("null") @Override public void
            mouseEntered(MouseEvent arg0) {
        IAsyncUITask t = null;
        t.doStuff();
    }
}
