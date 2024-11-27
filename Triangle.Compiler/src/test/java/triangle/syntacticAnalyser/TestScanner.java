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

	/**
	 * Tests for different cases, checking that they are digits
	 */
	@Test
	public void testIsDigit() {
		assertTrue(Scanner.isDigit('0')); //checking for 0
		assertTrue(Scanner.isDigit('1')); //checking for 1
		assertTrue(Scanner.isDigit('2')); //checking for 2
		assertTrue(Scanner.isDigit('3')); //checking for 3
		assertTrue(Scanner.isDigit('4')); //checking for 4
		assertTrue(Scanner.isDigit('5')); //checking for 5
		assertTrue(Scanner.isDigit('6')); //checking for 6
		assertTrue(Scanner.isDigit('7')); //checking for 7
		assertTrue(Scanner.isDigit('8')); //checking for 8
		assertTrue(Scanner.isDigit('9')); //checking for 9
		assertFalse(Scanner.isDigit('a'));
		assertFalse(Scanner.isDigit('Z'));
		assertFalse(Scanner.isDigit('&'));
		assertFalse(Scanner.isDigit(';'));
		assertFalse(Scanner.isDigit('\n'));
	}

	/**
	 * Tests for different cases, checking that they are operators
	 */
	@Test
	public void testIsOperator() {
		assertTrue(Scanner.isOperator('*'));
		assertTrue(Scanner.isOperator('/'));
		assertTrue(Scanner.isOperator('?'));
		assertTrue(Scanner.isOperator('+'));
		assertTrue(Scanner.isOperator('-'));
		assertTrue(Scanner.isOperator('\\')); //not Decl
		assertTrue(Scanner.isOperator('<')); //less Decl
		assertTrue(Scanner.isOperator('>')); //greater Decl
		assertTrue(Scanner.isOperator('&')); //and Decl
		assertTrue(Scanner.isOperator('=')); //equal Decl
		assertTrue(Scanner.isOperator('@'));
		assertTrue(Scanner.isOperator('%'));
		assertTrue(Scanner.isOperator('^'));
		assertTrue(Scanner.isOperator('?'));
		assertFalse(Scanner.isOperator('a'));
		assertFalse(Scanner.isOperator('b'));
		assertFalse(Scanner.isOperator('C'));
		assertFalse(Scanner.isOperator('d'));
		assertFalse(Scanner.isOperator('E'));
		assertFalse(Scanner.isOperator('f'));
		assertFalse(Scanner.isOperator('F'));
		assertFalse(Scanner.isOperator('Z'));
		assertFalse(Scanner.isOperator('1'));
		assertFalse(Scanner.isOperator('2'));
		assertFalse(Scanner.isOperator('3'));
		assertFalse(Scanner.isOperator('4'));
		assertFalse(Scanner.isOperator(';'));
		assertFalse(Scanner.isOperator('\n'));
	}

	/**
	 * Tests for different cases, checking that they are letters
	 */
	@Test
	public void testIsLetter() {

		// Test for lowercase letters
		assertTrue(Scanner.isLetter('A'));
		assertTrue(Scanner.isLetter('B'));
		assertTrue(Scanner.isLetter('C'));
		assertTrue(Scanner.isLetter('D'));
		assertTrue(Scanner.isLetter('E'));
		assertTrue(Scanner.isLetter('F'));
		assertTrue(Scanner.isLetter('G'));
		assertTrue(Scanner.isLetter('H'));
		assertTrue(Scanner.isLetter('I'));
		assertTrue(Scanner.isLetter('R'));
		assertTrue(Scanner.isLetter('S'));
		assertTrue(Scanner.isLetter('T'));
		assertTrue(Scanner.isLetter('U'));
		assertTrue(Scanner.isLetter('V'));
		assertTrue(Scanner.isLetter('W'));
		assertTrue(Scanner.isLetter('X'));
		assertTrue(Scanner.isLetter('Y'));
		assertTrue(Scanner.isLetter('Z'));
		assertFalse(Scanner.isLetter('^'));
		assertFalse(Scanner.isLetter('\n'));

		// Test for non letters
        assertFalse(Scanner.isLetter('='));
		assertFalse(Scanner.isLetter('&'));
		assertFalse(Scanner.isLetter('1'));

		// Test for lowercase letters
		assertTrue(Scanner.isLetter('a'));
		assertTrue(Scanner.isLetter('b'));
		assertTrue(Scanner.isLetter('c'));
		assertTrue(Scanner.isLetter('d'));
		assertTrue(Scanner.isLetter('e'));
		assertTrue(Scanner.isLetter('j'));
		assertTrue(Scanner.isLetter('k'));
		assertTrue(Scanner.isLetter('l'));
		assertTrue(Scanner.isLetter('m'));
		assertTrue(Scanner.isLetter('n'));
		assertTrue(Scanner.isLetter('o'));
		assertTrue(Scanner.isLetter('p'));
		assertTrue(Scanner.isLetter('q'));
		assertTrue(Scanner.isLetter('z'));
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

	/**
	 * Tests that the file square.trie compiles and runs successfully
	 */
	@Test
	public void testSquare() {
		compileExpectSuccess("/square.tri");
	}

	@Test
	public void testDoWhileDo() {
		compileExpectSuccess("/loopwhile.tri");
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
