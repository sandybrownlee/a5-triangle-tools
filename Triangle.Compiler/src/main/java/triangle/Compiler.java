/*
 * @(#)Compiler.java                       
 * 
 * Revisions and updates (c) 2022-2024 Sandy Brownlee. alexander.brownlee@stir.ac.uk
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

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

	@Argument(alias = "o", description = "The name of the file containing the object program", required = true, prefix = "--")
	private static String objectName = "obj.tam";

	@Argument(alias = "tree", description = "If true, then ABS (Abstract Syntax Tree) is to be displayed after\r\n"
			+ " contextual analysis", required = false, prefix = "--")
	private static boolean showTree;

	@Argument(alias = "fold", description = "Use folding", required = false, prefix = "--")
	private static boolean folding;

	@Argument(alias = "treeAfter", description = "Show ABS (Abstract Syntax Tree) after folding", required = false, prefix="--")
	private static boolean showTreeAfter = false;

	@Argument(alias = "stats", description = "Prints out the count lists", required = false, prefix = "--")
	private static boolean showStats;

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	private static SummaryVisitor summaryStats;

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName   the name of the file containing the source program.
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName, boolean showingTable) {
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
			if (showTree) {
				drawer.draw(theAST);
			}
			if (folding) {
				theAST.visit(new ConstantFolder());
				if (showTreeAfter) {
					drawer.draw(theAST);
				}
			}
			
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
			}
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			if (showStats) {
				summaryStats = new SummaryVisitor();
				theAST.visit(summaryStats);
				System.out.println("Summary statistics:");
				System.out.printf("There are %d binary expressions.\n", summaryStats.getBinaryExpressionsCount());
				System.out.printf("There are %d if commands.\n", summaryStats.getConditionalCommandsCount());
				System.out.printf("There are %d while expressions.\n", summaryStats.getWhileCommandsCount());
			}
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
		if (args.length < 1) {
			System.out.println("Usage: tc filename [-o=outputfilename] [tree] [folding]");
			System.exit(1);
		}

		String sourceName = args[0];

	    // this will parse the list of arguments passed into the program, and
	    // populate the appropriate instance variables
	    // if the required arguments were not found, it will gracefully exit 
	    Args.parseOrExit(Compiler.class, args);

		var compiledOK = compileProgram(sourceName, false);

		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}
}
