package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class TestScanner {

	/* some individual unit tests for helper methods in Scanner */

	@Test
	public void testIsDigit() {
		assertTrue(Scanner.isDigit('0'));
		assertTrue(Scanner.isDigit('1'));
		assertTrue(Scanner.isDigit('5'));
		assertTrue(Scanner.isDigit('8'));
		assertTrue(Scanner.isDigit('9'));
		assertFalse(Scanner.isDigit('a'));
		assertFalse(Scanner.isDigit('Z'));
		assertFalse(Scanner.isDigit('&'));
		assertFalse(Scanner.isDigit(';'));
		assertFalse(Scanner.isDigit('\n'));
	}

	@Test
	public void testIsOperator() {
		// Existing tests
		assertTrue(Scanner.isOperator('*'));
		assertTrue(Scanner.isOperator('/'));
		assertTrue(Scanner.isOperator('?'));
		assertTrue(Scanner.isOperator('+'));
		assertTrue(Scanner.isOperator('-'));

		// Additional tests for edge cases
		assertTrue(Scanner.isOperator('='));  // '=' is an operator
		assertTrue(Scanner.isOperator('<'));  // '<' is an operator
		assertTrue(Scanner.isOperator('>'));  // '>' is an operator
		assertTrue(Scanner.isOperator('&'));  // '&' is an operator

		assertFalse(Scanner.isOperator('a'));  // 'a' is not an operator
		assertFalse(Scanner.isOperator('Z'));  // 'Z' is not an operator
		assertFalse(Scanner.isOperator('1'));  // '1' is not an operator
		assertFalse(Scanner.isOperator(';'));  // ';' is not an operator
		assertFalse(Scanner.isOperator('\n')); // Newline is not an operator
		assertFalse(Scanner.isOperator('@'));  // '@' is not an operator
	}

	@Test
	public void testIsLetter() {
		// Test for all alphabetic characters
		for (char c = 'a'; c <= 'z'; c++) {
			assertTrue(Scanner.isLetter(c));  // Lowercase letters should be true
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			assertTrue(Scanner.isLetter(c));  // Uppercase letters should be true
		}

		// Test for non-letter characters
		assertFalse(Scanner.isLetter('1'));  // Digits should be false
		assertFalse(Scanner.isLetter('@'));  // Symbols should be false
		assertFalse(Scanner.isLetter('*'));  // Special characters should be false
		assertFalse(Scanner.isLetter(';'));  // Punctuation should be false
		assertFalse(Scanner.isLetter('\n')); // Newline should be false
	}

	/* these tests all try to compile example programs... */

    /*@Test
    public void testHi() {
        compileExpectSuccess("/hi.tri");
    }
    */

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
		SourceFile source = SourceFile.fromResource(filename);
		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);

		parser.parseProgram();
		assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}

	private void compileExpectFailure(String filename) {
		SourceFile source = SourceFile.fromResource(filename);
		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);

		assertThrows(RuntimeException.class, new ThrowingRunnable() {
			public void run() {
				parser.parseProgram();
			}
		});

		assertNotEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}
}
