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
	 private final char[] operators = {'+', '-', '*', '/', '=', '<', '>', '\\', '&', '@', '%', '^', '?'};
	 private final 	char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/* some individual unit tests for helper methods in Scanner */
	@Test
	public void testIsLetter(){
		//test all lower case letters
		for (char c = 'a'; c <= 'z'; c++) {
			assertTrue("Expected " + c + " to be a letter", Scanner.isLetter(c));
		}
		// test all uppercase letter
		for (char c = 'A'; c <= 'Z'; c++) {
			assertTrue("Expected " + c + " to be a letter", Scanner.isLetter(c));
		}
		//test for non letters
		char[] nonLetters = {'1', '9', '*', '_', '$', ' '};
		for (char nonLetter : nonLetters) {
			assertFalse("Expected " + nonLetter + " to not be a letter", Scanner.isLetter(nonLetter));
		}
	}

	@Test
	public void testIsDigit() {
		for (char op : digits) {
			assertTrue("Expected " + op + " to be an digit", Scanner.isDigit(op));
		}
		assertFalse(Scanner.isDigit('a'));
		assertFalse(Scanner.isDigit('Z'));
		assertFalse(Scanner.isDigit('&'));
		assertFalse(Scanner.isDigit(';'));
		assertFalse(Scanner.isDigit('\n'));
	}

	@Test
	public void testIsOperator() {
		//char[] operators = {'+', '-', '*', '/', '=', '<', '>', '\\', '&', '@', '%', '^', '?'};
		for (char op : operators) {
			assertTrue("Expected " + op + " to be an operator", Scanner.isOperator(op));
		}
		assertFalse(Scanner.isOperator('a'));
		assertFalse(Scanner.isOperator('Z'));
		assertFalse(Scanner.isOperator('1'));
		assertFalse(Scanner.isOperator(';'));
		assertFalse(Scanner.isOperator('\n'));
	}


	/* these tests all try to compile example programs... */

	@Test
	public void testHi() {
		compileExpectSuccess("/hi.tri");
	}

	@Test

	public void testLoopWHile() {
		compileExpectSuccess("/LoopWhileTest.tri");
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


	/**
	 * Test that the square shotcut command (a**) compiles and runs
	 */
	@Test
	public void testSquareShortcutCommand() {
		compileExpectSuccess("/squarenum.tri");
	}



	private void compileExpectSuccess(String filename) {
		// build.gradle has a line sourceSets.test.resources.srcDir file("$rootDir/programs")
		// which adds the programs directory to the list of places Java can easily find files
		// getResource() below searches for a file, which is in /programs
		//SourceFile source = SourceFile.ofPath(this.getClass().getResource(filename).getFile().toString());
		SourceFile source = SourceFile.fromResource(filename);

		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);

		parser.parseProgram();

		// we should get to here with no exceptions

		assertEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}

	private void compileExpectFailure(String filename) {
		//SourceFile source = SourceFile.ofPath(this.getClass().getResource(filename).getFile().toString());
		SourceFile source = SourceFile.fromResource(filename);
		Scanner scanner = new Scanner(source);
		ErrorReporter reporter = new ErrorReporter(true);
		Parser parser = new Parser(scanner, reporter);

		// we expect an exception here as the program has invalid syntax
		assertThrows(RuntimeException.class, new ThrowingRunnable() {
			public void run(){
				parser.parseProgram();
			}
		});

		// currently this program will fail
		assertNotEquals("Problem compiling " + filename, 0, reporter.getNumErrors());
	}

}