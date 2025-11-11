package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class TestDoubleCommand {

    @Test
    public void testDoubleCommandShorthand() {
        // Compiles the program that uses the shorthand doubling command: a**;
        SourceFile source = SourceFile.fromResource("/double.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        parser.parseProgram();

        assertEquals("Problem compiling /double.tri", 0, reporter.getNumErrors());
    }
}