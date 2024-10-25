package triangle.syntacticAnalyser;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Lexer;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.SyntaxError;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestScanner {

    /* these tests all try to compile example programs... */

    @Test
    public void testHi() {
        compileExpectSuccess("/hi.tri");
    }


    @Test
    public void testHiNewComment() {
        compileExpectFailure("/hi-newcomment.tri");
    }


    @Test
    public void testHiNewComment2() {
        compileExpectFailure("/hi-newcomment2.tri");
    }


    @Test
    public void testBarDemo() {
        compileExpectFailure("/bardemo.tri");
    }


    @Test
    public void testRepeatUntil() {
        compileExpectFailure("/repeatuntil.tri");
    }


    private void compileExpectSuccess(String filename) {
        // build.gradle has a line sourceSets.test.resources.srcDir file("$rootDir/programs")
        // which adds the programs directory to the list of places Java can easily find files
        // getResource() below searches for a file, which is in /programs
        //SourceFile source = SourceFile.ofPath(this.getClass().getResource(filename).getFile().toString());

        Lexer lexer = Lexer.fromResource(filename);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(lexer, reporter);

        try {
            parser.parseProgram();
        } catch (IOException e) {
            throw new AssertionFailedError();
        } catch (SyntaxError e) {
            throw new AssertionFailedError();
        }

        // we should get to here with no exceptions

        assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
    }

    private void compileExpectFailure(String filename) {
        Lexer lexer = Lexer.fromResource(filename);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(lexer, reporter);

        // we expect an exception here as the program has invalid syntax
        assertThrows(RuntimeException.class, () -> {
            try {
                parser.parseProgram();
            } catch (IOException | SyntaxError e) {
                throw new RuntimeException();
            }
        });

        // currently this program will fail
        assertNotEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
    }

}
