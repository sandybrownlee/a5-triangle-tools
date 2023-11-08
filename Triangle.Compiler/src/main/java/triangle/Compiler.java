/*
 * @(#)Compiler.java                       
 * 
 * Revisions and updates (c) 2022-2023 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 * 
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.WhileHoister;
import triangle.optimiser.ExpressionStatistics;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

	/** Command line arguments */
	@Argument(alias = "s", description = "The source file to compile", required = true)
	protected static String sourceName = "programs/file.tri";

	@Argument(alias = "o", description = "Outputs the compiled TAM file in the CWD to this name [i.e. x.tam]", required = false)
	protected static String outputName;

	@Argument(alias = "fold", description = "Enable constant folding", required = false)
	protected static Boolean folding = false;

	@Argument(alias = "sAst", description = "Show AST before optimisation", required = false)
	protected static Boolean showUnoptimisedTree = false;

	@Argument(alias ="sfAst", description = "Show AST after folding", required = false)
	protected static Boolean showFoldedTree = false;

	@Argument(alias = "stats", description = "Print number of integer and character expressions after any applied optimisation", required = false)
	protected static Boolean showExpressionStatistics = false;
	
	@Argument(description = "Enable hoisting", required = false)
	protected static Boolean hoist = false;
	
	@Argument(alias = "shAst", description = "Show AST after folding and or hoisting", required = false)
	protected static Boolean showHoistedTree = false;


	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	/**
	 * Compile the source program to TAM machine code.
	 * 
	 *
	 * //@param sourceName   the name of the file containing the source program.
	 * //@param objectName   the name of the file containing the object program.
	 * @param showingAST   true iff the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */




	static boolean compileProgram(String sourceName, String objectName, boolean fold, boolean hoist, boolean showingStats) {

		System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceName);

		if (source == null) {
			System.out.println("Can't access source file " + sourceName);
			System.exit(1);
		}

		scanner = new Scanner(source);
		reporter = new ErrorReporter(false);
		parser = new Parser(scanner, reporter);
		checker = new Checker(reporter);
		emitter = new Emitter(reporter);
		encoder = new Encoder(emitter, reporter);
		drawer = new Drawer();
		ExpressionStatistics expressionStats = new ExpressionStatistics();

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass

			

			if (showUnoptimisedTree) {
				drawer.draw(theAST);
			}
			
			if (fold) {
				theAST.visit(new ConstantFolder());
			}
			
			
			if (showFoldedTree) {
				if (!fold) {
					System.out.println("Folding NOT enabled, so tree cannot be ");
				} else {
					drawer.draw(theAST);
				}
				
			}
			
			if (hoist) {				
				theAST.visit(new WhileHoister());
			}
			
			if (showHoistedTree) {
				if (!hoist) {
					System.out.println("Hoisting NOT enabled, so tree cannot be shown");
					
				} else {
					if (fold) { System.out.println("Warning: Hoisted AST has also been folded"); }
					drawer.draw(theAST);
				}
				
			}
			
			if (showingStats) {
				expressionStats.visitProgram(theAST, null);
			 }
			
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, false); // 3rd pass
			}
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else {
			System.out.println("Compilation was unsuccessful.");
		}
		return successful;
	}

	/**
	 * Triangle compiler main program.
	 *
	 * @param args the only command-line argument to the program specifies the
	 *             source filename.
	 */
	public static void main(String[] args) {
		// NEW
		Compiler compiler = new Compiler();
		Args.parseOrExit(compiler, args);
		
		var compiledOK = compileProgram(sourceName, outputName, folding, hoist, showExpressionStatistics);
		
		if (!showUnoptimisedTree && !showFoldedTree && !showHoistedTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}


}
