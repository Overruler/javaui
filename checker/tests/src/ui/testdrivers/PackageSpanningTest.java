package ui.testdrivers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import checkers.util.test.CheckerParameterized;
import checkers.util.test.CheckerTest;
import checkers.util.test.TestUtilities;

/**
 */
@RunWith(CheckerParameterized.class)
public class PackageSpanningTest extends CheckerTest {
    private static final String FILES_DIR = "ui" + File.separator
        + "packagespanning";

    public PackageSpanningTest(@SuppressWarnings("unused") File testFile) {
        super(
            guitypes.checkers.GUIEffectsChecker.class.getName(),
            FILES_DIR,
            //"-AprintErrorStack",
            //"-Alint=debugSpew",
            "-Anomsgtext");
    }

    @Test public void run() {
        List<Object[]> files = testFiles(FILES_DIR);
        File[] testFiles = combine(files);
        test(checkerName, checkerOptions, testFiles);
    }

    @Parameters public static Collection<Object[]> data() {
        List<Object[]> arguments = testFiles(FILES_DIR);
        if(arguments.size() > 0) {
            arguments.removeAll(arguments.subList(1, arguments.size()));
        }
        return arguments;
    }

    private static File[] combine(Collection<Object[]> files) {
        Collection<File> listing = new ArrayList<>();
        for(Object[] object : files) {
            for(Object object2 : object) {
                listing.add((File) object2);
            }
        }
        File[] argument = listing.toArray(new File[listing.size()]);
        return argument;
    }

    protected static List<Object[]> testFiles(String... folders) {
        List<Object[]> arguments = new ArrayList<>();
        for(String folder : folders) {
            File dir = new File("tests" + File.separator + folder);
            List<File> javaFiles =
                TestUtilities.deeplyEnclosedJavaTestFiles(dir);

            for(File javaFile : javaFiles) {
                arguments.add(new Object[] {
                    javaFile
                });
            }
        }
        return arguments;
    }
}
