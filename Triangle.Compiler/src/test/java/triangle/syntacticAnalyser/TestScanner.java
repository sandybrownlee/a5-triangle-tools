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
	public void testIsLetter() {
		assertFalse(Scanner.isLetter('0'));
		assertTrue(Scanner.isLetter('a'));
		assertTrue(Scanner.isLetter('A'));
		assertFalse(Scanner.isLetter('\n'));
	}
	
	@Test
	public void testIsOperator() {
		assertTrue(Scanner.isOperator('*'));
		assertTrue(Scanner.isOperator('<'));
		assertTrue(Scanner.isOperator('>'));
		assertTrue(Scanner.isOperator('&'));
		assertTrue(Scanner.isOperator('/'));
		assertTrue(Scanner.isOperator('?'));
		assertTrue(Scanner.isOperator('+'));
		assertTrue(Scanner.isOperator('-'));
		assertTrue(Scanner.isOperator('%'));
		assertTrue(Scanner.isOperator('@'));
		assertTrue(Scanner.isOperator('^'));
		assertFalse(Scanner.isOperator('.'));
		assertFalse(Scanner.isOperator(' '));
		assertFalse(Scanner.isOperator('a'));
		assertFalse(Scanner.isOperator('Z'));
		assertFalse(Scanner.isOperator('1'));
		assertFalse(Scanner.isOperator(';'));
		assertFalse(Scanner.isOperator('\n'));
		assertFalse(Scanner.isOperator('\t'));
		assertTrue(Scanner.isOperator('\\'));
	}


	@Test
	public void testExpectedSuccessfulCompilation() {
		compileExpectSuccess("/adddeep.tri");
		compileExpectSuccess("/booleans-to-fold.tri");
		compileExpectSuccess("/procedural.tri");
		compileExpectSuccess("/directories.tri");
		compileExpectSuccess("/functions.tri");
		compileExpectSuccess("/square.tri");
		compileExpectSuccess("/hi.tri");
		compileExpectSuccess("/nesting.tri");
		compileExpectSuccess("/procedures.tri");
		compileExpectSuccess("/while-to-hoist.tri"); // this is kind of bait in that it its only testing without -hoist as an argument
	}


	@Test
	public void testExpectedFailedCompilation() {
		compileExpectFailure("/bardemo.tri");
		compileExpectFailure("/increment.tri");
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
