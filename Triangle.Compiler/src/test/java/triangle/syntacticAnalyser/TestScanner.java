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

	@Test
	public void testIsLetter() {
		// Lowercase letters
		assertTrue(Scanner.isLetter('a'));
		assertTrue(Scanner.isLetter('m'));
		assertTrue(Scanner.isLetter('z'));

		// Uppercase letters (Cover all boundaries)
		assertTrue(Scanner.isLetter('A'));
		assertTrue(Scanner.isLetter('M'));
		assertTrue(Scanner.isLetter('Z'));

		// Invalid cases
		assertFalse(Scanner.isLetter('1'));  // Digit
		assertFalse(Scanner.isLetter(' '));  // Whitespace
		assertFalse(Scanner.isLetter('\n')); // Newline
		assertFalse(Scanner.isLetter('&'));  // Special character
		assertFalse(Scanner.isLetter('@'));  // Edge special character
		assertFalse(Scanner.isLetter('['));  // Character just beyond 'Z'
		assertFalse(Scanner.isLetter('`'));  // Character just before 'a'
		assertFalse(Scanner.isLetter('{'));  // Character just after 'z'
		assertFalse(Scanner.isLetter('\0')); // Null character
	}

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
		assertTrue(Scanner.isOperator('%'));
		assertTrue(Scanner.isOperator('*'));
		assertTrue(Scanner.isOperator('/'));
		assertTrue(Scanner.isOperator('\\'));
		assertTrue(Scanner.isOperator('?'));
		assertTrue(Scanner.isOperator('+'));
		assertTrue(Scanner.isOperator('-'));
		assertTrue(Scanner.isOperator('='));
		assertTrue(Scanner.isOperator('&'));
		assertTrue(Scanner.isOperator('<'));
		assertTrue(Scanner.isOperator('>'));
		assertTrue(Scanner.isOperator('@'));
		assertTrue(Scanner.isOperator('^'));


		assertFalse(Scanner.isOperator('a'));
		assertFalse(Scanner.isOperator('Z'));
		assertFalse(Scanner.isOperator('1'));
		assertFalse(Scanner.isOperator(';'));
		assertFalse(Scanner.isOperator(' ')); // Space
		assertFalse(Scanner.isOperator('\n'));
		assertFalse(Scanner.isOperator('\0')); // Null character


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

	// Task 3b test for square operation
	@Test
	public void testSquare() {
		compileExpectSuccess("/taskBtest.tri");
	}

	// Task 6b test for new loop
	@Test
	public void testLoopWhile() {
		compileExpectSuccess("/taskBloop.tri");
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
