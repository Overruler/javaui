package ui.testdrivers;

import java.io.File;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * JUnit tests for the GUI Effects Checkers.
 */
public class ManualTestRunnerForIDE {
    protected static String checkerName =
        "guitypes.checkers.GUIEffectsChecker";
    private static final Class<?> TEST_CLASS = Tests4Driver.class;
    private static final String TESTS_CLASSPATH = "tests.classpath";

    public static void main(String[] args) {
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        System.setErr(System.out);
        String classpath = System.getProperty(TESTS_CLASSPATH, "");
        System.setProperty(TESTS_CLASSPATH, classpath + File.pathSeparator
            + "bin" + File.pathSeparator + "bin-tests");
        System.out.println("Run starting");
        Result run = jc.run(TEST_CLASS);

        if(run.wasSuccessful()) {
            System.out.println("Run was successful with "
                + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount()
                + " failure(s) out of " + run.getRunCount() + " run(s)!");

            for(Failure f : run.getFailures()) {
                System.out.println(f.toString());
                Throwable exception = f.getException();
                if(exception != null) {
                    exception.printStackTrace();
                }
            }
        }
    }

    //public static class AndroidFenumCheckerTests extends ParameterizedCheckerTest {
    //    public AndroidFenumCheckerTests(File testFile) {
    //        super(testFile, AndroidTests.checkerName, "sparta.checkers", "-Anomsgtext");
    //    }
    //    @Parameters
    //    public static Collection<Object[]> data() {
    //        return testFiles("fenums");
    //    }
    //}

    //public static class AndroidReqPermissionsCheckerTests extends ParameterizedCheckerTest {
    //    public AndroidReqPermissionsCheckerTests(File testFile) {
    //        super(testFile, AndroidTests.checkerName, "sparta.checkers", "-Anomsgtext");
    //    }
    //    @Parameters
    //    public static Collection<Object[]> data() {
    //        return testFiles("reqperms");
    //    }
    //}
}
