package triangle.syntacticAnalyser;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Lexer;
import triangle.syntacticAnalyzer.Parser;

import static org.junit.Assert.*;

public class TestScanner {

    /* some individual unit tests for helper methods in Lexer */

    @Test
    public void testIsDigit() {
        assertTrue(Lexer.isDigit('0'));
        assertTrue(Lexer.isDigit('1'));
        assertTrue(Lexer.isDigit('5'));
        assertTrue(Lexer.isDigit('8'));
        assertTrue(Lexer.isDigit('9'));
        assertFalse(Lexer.isDigit('a'));
        assertFalse(Lexer.isDigit('Z'));
        assertFalse(Lexer.isDigit('&'));
        assertFalse(Lexer.isDigit(';'));
        assertFalse(Lexer.isDigit('\n'));
    }

    @Test
    public void testIsOperator() {
        assertTrue(Lexer.isOperator('*'));
        assertTrue(Lexer.isOperator('/'));
        assertTrue(Lexer.isOperator('?'));
        assertTrue(Lexer.isOperator('+'));
        assertTrue(Lexer.isOperator('-'));
        assertFalse(Lexer.isOperator('a'));
        assertFalse(Lexer.isOperator('Z'));
        assertFalse(Lexer.isOperator('1'));
        assertFalse(Lexer.isOperator(';'));
        assertFalse(Lexer.isOperator('\n'));
    }


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

        parser.parseProgram();

        // we should get to here with no exceptions

        assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
    }

    private void compileExpectFailure(String filename) {
        //SourceFile source = SourceFile.ofPath(this.getClass().getResource(filename).getFile().toString());
        Lexer lexer = Lexer.fromResource(filename);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(lexer, reporter);

        // we expect an exception here as the program has invalid syntax
        assertThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() {
                parser.parseProgram();
            }
        });

        // currently this program will fail
        assertNotEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
    }

}
