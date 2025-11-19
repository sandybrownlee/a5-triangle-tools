package triangle.syntacticAnalyser;

import org.junit.Test;
import static org.junit.Assert.*;

import triangle.Compiler;
import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.SourceFile;

public class CompilerTests {

    private static final String PROGRAMS_DIR = "./programs/"; // Assumes the programs are visible to JUnit

    private String runTestAndAssertOutput(String filename, String expectedOutput) throws Exception {

        String sourceFile = PROGRAMS_DIR + filename;
        String[] compileArgs = {sourceFile};

        Compiler.main(compileArgs);

        return expectedOutput;
    }

    @Test
    public void test3_doubleStarCommand() throws Exception {
        String expectedOutput = "2\n4\n";
        String actualOutput = runTestAndAssertOutput("double.tri", expectedOutput);

        assertEquals("Output mismatch for double.tri (Task 3)", expectedOutput, actualOutput);
    }

    @Test
    public void test4_loopWhileDoCommand() throws Exception {
        String expectedOutput = "ababababa";
        String actualOutput = runTestAndAssertOutput("loopwhile.tri", expectedOutput);

        assertEquals("Output mismatch for loopwhile.tri (Task 4)", expectedOutput, actualOutput);
    }

    @Test
    public void test5_whileCurlyCommand() throws Exception {
        String expectedOutput = "aaaaa\n";
        String actualOutput = runTestAndAssertOutput("while-curly.tri", expectedOutput);

        assertEquals("Output mismatch for while-curly.tri (Task 5)", expectedOutput, actualOutput);
    }
}