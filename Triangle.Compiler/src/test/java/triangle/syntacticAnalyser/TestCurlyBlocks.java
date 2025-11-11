package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class TestCurlyBlocks {

    @Test
    public void testCurlyBlocksWhile() {
        // programs directory is mapped as test resources in build.gradle
        SourceFile source = SourceFile.fromResource("/while-curly.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        parser.parseProgram();

        assertEquals("Problem compiling /while-curly.tri", 0, reporter.getNumErrors());
    }
}