package ui.testdrivers;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import checkers.util.test.CheckerParameterized;
import checkers.util.test.ParameterizedCheckerTest;

/**
 */
@RunWith(CheckerParameterized.class)
public class ExtraTest extends ParameterizedCheckerTest {

    public ExtraTest(File testFile) {
        // TODO: This "guieffects" is supposed to the the "String checkerDir" in the parent class, and should probably be tests/ui or ui or something like that
        // Note that several superclasses up, it hardcodes "tests"+File.separator+checkerDir
        super(
            testFile,
            guitypes.checkers.GUIEffectsChecker.class.getName(),
            "ui" + File.separator + "extra",
            "-Xbootclasspath/a:tests/build/testclasses",
            "-Anomsgtext");
        //"-Anomsgtext", "-Alint=debugSpew");
        //System.out.println("Kicking off UITestDriver");
    }

    @Parameters public static Collection<Object[]> data() {
        //System.out.println("Getting test data dirs");
        return testFiles("ui" + File.separator + "extra");
    }
}
