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
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import triangle.optimiser.SummaryStats; // import new class like the rest

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {


	/** The filename for the object program, normally obj.tam. */
	//static String objectName = "obj.tam";
	
	//static boolean showTree = false;
	//static boolean folding = false;

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	// the cli parser library lets us make instance variables with annotations like
	// this
	// that specify command line arguments for the program

	// Task 2B Ammendments:
	@Argument(alias = "sourceName", description = "Name of the file containing the source program", required = true)
	static String sourceName = "while-curly.tri";

	@Argument(alias = "objName", description = "Name of the file containing the object program", required = true)
	static String objectName = "obj.tam";

	@Argument(alias = "showAST", description = "Show/display the AST")
	static boolean showTree = false;

	@Argument(alias = "foldAST", description = "Fold the AST")
	static boolean folding = false;

	@Argument(alias = "showFoldedAST", description = "Show the AST after folding")
	static boolean showTreeAfterFolding = false;

	// Task 5B: Add the option of 'stats' for when running the compiler
	@Argument(alias = "stats", description = "Show number of CharacterExpressions and IntegerExpressions")
	static boolean stats = false;

	public static void main(String[] args) {
	    Compiler compiler = new Compiler();

		// this will parse the list of arguments passed into the program, and
	    // populate the appropriate instance variables
	    // if the required arguments were not found, it will gracefully exit 
	    Args.parseOrExit(compiler, args);
	    
		if (args.length > 0) {
			var compiledSuccessfully = compileProgram(sourceName, objectName, showTree, folding, showTreeAfterFolding, stats);

			if (!showTree) {
				System.exit(compiledSuccessfully ? 0 : 1);
			}
		}
		else{
			System.out.println("Need arguments");
			System.exit(1);
		}
	
	}

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName   the name of the file containing the source program.
	 * @param objectName   the name of the file containing the object program.
	 * @param showingAST   true iff the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * * @param showStats 		show stats
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable, boolean showTreeAfterFolding, boolean showStats) {

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

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass
			if (showingAST) {
				drawer.draw(theAST);
			}
			if (folding) {
				theAST.visit(new ConstantFolder());
				// Task 2C changes:
				System.out.println("This is the AST after folding...");
				drawer.draw(theAST);
				showTree = true;
			}
			// Task 5B: Adding stats option implementation, will print out smmary of stats to screen after visiting the AST
			if (stats) {
				SummaryStats statsSummary = new SummaryStats();
				theAST.visit(statsSummary);
				System.out.println("This is the Summary of stats: " + "\n" + "Character Expressions Counter: " + statsSummary.getNumberCharacterExpressions() + "\n" + "Integer Expressions Counter: " + statsSummary.getNumberIntegerExpressions());
			}
			
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
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
	
	// private static void parseArgs(String[] args) {
	// 	for (String s : args) {
	// 		var sl = s.toLowerCase();
	// 		if (sl.equals("tree")) {
	// 			showTree = true;
	// 		} else if (sl.startsWith("-o=")) {
	// 			objectName = s.substring(3);
	// 		} else if (sl.equals("folding")) {
	// 			folding = true;
	// 		}
	// 	}
	// }
}
