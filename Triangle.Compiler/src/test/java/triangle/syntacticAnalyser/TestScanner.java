package triangle.syntacticAnalyser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;


public class TestScanner {

	@Test
	public void testHi() {
		compileExpectSuccess("/hi.tri");
	}
	

	@Test
	public void testHiNewComment() {
		compileExpectSuccess("/hi-newcomment.tri");
	}
	

	@Test
	public void testHiNewComment2() {
		compileExpectSuccess("/hi-newcomment2.tri");
	}
	

	@Test
	public void testBarDemo() {
		compileExpectSuccess("/bardemo.tri");
	}
	

	@Test
	public void testRepeatUntil() {
		compileExpectSuccess("/repeatuntil.tri");
	}

	@Test
	public void testAdd() {
		compileExpectSuccess("/add.tri");
	}
	@Test
	public void testSquare() {
		compileExpectSuccess("/square.tri");
	}

	@Test
	public void testWhileDo() {
		compileExpectSuccess("/While-longloop.tri");
	}

	@Test
	public void testIsOperator(){
		//initialize variables and class instances
		SourceFile source = SourceFile.fromResource("/testDigOp.txt");
		Scanner scanner = new Scanner(source);
		String operators = "+-*/=<>\\&@%^?|";
		String nonOperators = "12345abcde{}[].,~()";
		char currentChar = ' ';
		Integer trueCount = 0;
		Integer falseCount= 0;
		
			for(int i = 0; i<operators.length(); i++){ //iterates through operator string, and passing it to Isoperator():expects true
				currentChar = operators.charAt(i);
				if(scanner.testIsOperator(currentChar)){ //if current character isOperator() increase true count
					trueCount++;
				}
			}
			for(int i = 0; i<nonOperators.length(); i++){
				currentChar = nonOperators.charAt(i);
				if(!scanner.testIsOperator(currentChar)){ //if current character isn't an Operator increase false count
					falseCount++;
				}
			}
		
		assertEquals("Failed to count correct number of operators",(Integer)14,trueCount); 
		assertEquals("Failed to count correct number of non-operators",(Integer)19,falseCount);
	}

	@Test
	public void testIsDigit(){
		//initialize variables and class instances
		SourceFile source = SourceFile.fromResource("/testDigOp.txt");
		Scanner scanner = new Scanner(source);
		String letters = "abcDEF";
		String nonLetter = "01234{}[].,~()$+-*><@# ;'";
		char currentChar = ' ';
		Integer trueCount = 0;
		Integer falseCount= 0;
		
		for(int i = 0; i<letters.length(); i++){ //iterates through the letters string and passing it to isLetter(): expects true
			currentChar = letters.charAt(i);
			if(scanner.testIsLetter(currentChar)){ //if current character isLetter() increase true count
				trueCount++;
			}
		}
		for(int i = 0; i<nonLetter.length(); i++){ //iterates through the non-letters string and passing it to isLetter(): expects false
			currentChar = nonLetter.charAt(i);
			if(!scanner.testIsLetter(currentChar)){
				falseCount++;
			}
		}

		assertEquals("Failed to count correct number of letters",(Integer)6, trueCount);
		assertEquals("Failed to count correct number of non-letters",(Integer)25,falseCount);
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
