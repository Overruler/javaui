package ui.testdrivers;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import checkers.util.test.CheckerParameterized;
import checkers.util.test.ParameterizedCheckerTest;

@RunWith(CheckerParameterized.class)
public class Tests4Driver extends ParameterizedCheckerTest {

    public Tests4Driver(File testFile) {
        // TODO: This "guieffects" is supposed to the the
        // "String checkerDir" in the parent class, and should
        // probably be tests/ui or ui or something like that
        // Note that several superclasses up, it hardcodes
        // "tests"+File.separator+checkerDir
        super(testFile, guitypes.checkers.GUIEffectsChecker.class
            .getName(), "ui" + File.separator + "tests4",
        //"-Alint=debugSpew",
            "-Anomsgtext");
    }

    @Parameters public static Collection<Object[]> data() {
        // System.out.println("Getting test data dirs");
        return testFiles("ui" + File.separator + "tests4");
    }
}
