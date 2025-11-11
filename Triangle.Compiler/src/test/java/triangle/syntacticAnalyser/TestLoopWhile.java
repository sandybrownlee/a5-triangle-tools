package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class TestLoopWhile {

    @Test
    public void testLoopWhileSyntaxCompiles() {
        // programs directory is mapped as test resources in build.gradle
        SourceFile source = SourceFile.fromResource("/loopwhile.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        parser.parseProgram();

        assertEquals("Expected loop-while example to compile with 0 errors", 0, reporter.getNumErrors());
    }
}