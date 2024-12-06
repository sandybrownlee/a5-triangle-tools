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
		// Test cases for digits '0' to '9'
		assertTrue(Scanner.isDigit('0'));
		assertTrue(Scanner.isDigit('1'));
		assertTrue(Scanner.isDigit('5'));
		assertTrue(Scanner.isDigit('8'));
		assertTrue(Scanner.isDigit('9'));
		// Test cases for non-digit characters
		assertFalse(Scanner.isDigit('a'));
		assertFalse(Scanner.isDigit('Z'));
		assertFalse(Scanner.isDigit('&'));
		assertFalse(Scanner.isDigit(';'));
		assertFalse(Scanner.isDigit('\n'));
	}
	
	@Test
	public void testIsOperator() {
		// Test cases for valid operators
		assertTrue(Scanner.isOperator('*'));
		assertTrue(Scanner.isOperator('/'));
		assertTrue(Scanner.isOperator('?'));
		assertTrue(Scanner.isOperator('+'));
		assertTrue(Scanner.isOperator('-'));
		// Test cases for non-operator characters
		assertFalse(Scanner.isOperator('a'));
		assertFalse(Scanner.isOperator('Z'));
		assertFalse(Scanner.isOperator('1'));
		assertFalse(Scanner.isOperator(';'));
		assertFalse(Scanner.isOperator('\n'));
	}

	@Test
	public void testIsLetter() {
		// Test cases for valid letters (both lowercase and uppercase)
		assertTrue(Scanner.isLetter('a'));
		assertTrue(Scanner.isLetter('z'));
		assertTrue(Scanner.isLetter('A'));
		assertTrue(Scanner.isLetter('Z'));
		// Test cases for non-letter characters
		assertFalse(Scanner.isLetter('1')); // Digit
		assertFalse(Scanner.isLetter('0')); // Digit
		assertFalse(Scanner.isLetter('&')); // Special character
		assertFalse(Scanner.isLetter(';')); // Special character
		assertFalse(Scanner.isLetter('\n')); // Newline character
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
		// Load the source file from the resources
		SourceFile source = SourceFile.fromResource(filename);
		
		// Create a new Scanner and Parser for the source file
		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);
		
		// Attempt to parse the program
		parser.parseProgram();
		
		// Assert that there are no compilation errors
		assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}
	
	private void compileExpectFailure(String filename) {
		// Load the source file from the resources
		SourceFile source = SourceFile.fromResource(filename);
		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);

		// Expect a RuntimeException due to invalid syntax
		assertThrows(RuntimeException.class, new ThrowingRunnable() {
			public void run(){
				parser.parseProgram();
			}
		});
		
		// Assert that there are compilation errors
		assertNotEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}
}