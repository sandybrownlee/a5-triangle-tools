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

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.Hoister;
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

	/** The filename for the object program, default obj.tam. */
	@Argument(alias = "o", description = "Output object name")
	static String objectName = "obj.tam";

	/** Flag to displays the AST after parsing program text. */
	@Argument(description = "Display the AST")
	static boolean showTree = false;

	/** Flag to apply constant folding optimisations. */
	@Argument(description = "Apply constant folding optimisation")
	static boolean folding = false;

	/** Flag to display AST after folding optimisations */
	@Argument(description = "Display AST after folding optimisations. This option is ignored if folding optimisation is disabled")
	static boolean showTreeAfter = false;

	/** Flag to display AST after folding optimisations */
	@Argument(description = "Print stats about the programs AST")
	static boolean showStats = false;

	/** Flag to apply invariant hoisting optimisations. */
	@Argument(description = "Apply invariant hoisting optimisation")
	static boolean hoisting = false;


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
	 * @param sourceName   the name of the file containing the source program.
	 * @param objectName   the name of the file containing the object program.
	 * @param showingAST   true iff the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable) {

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

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass

			if (showingAST) {
				drawer = new Drawer();
				drawer.draw(theAST);
			}

			if (folding) {
				theAST.visit(new ConstantFolder());

				if (showTreeAfter) {
					// Re instantiate drawer so if both pre and post folding trees are shown, they are different
					// Otherwise, two windows will open which display the same folded tree
					drawer = new Drawer();
					drawer.draw(theAST);
				}
			}

			if (hoisting) {
				theAST.visit(new Hoister());
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

		if (showStats) {
			SummaryVisitor summaryVisitor = new SummaryVisitor();
			System.out.println("\n" + summaryVisitor.createSummary(theAST));
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
		var sourceName = parseArgs(args);

		var compiledOK = compileProgram(sourceName, objectName, showTree, false);

		if (!(showTree || showTreeAfter)) {
			System.exit(compiledOK ? 0 : 1);
		}
	}

	/**
	 * Function to parse arguments and populate static flags and variables.
	 *
	 * @param args Program arguments to be parsed.
	 * @return Path to the source file to be compiled.
	 */
	private static String parseArgs(String[] args) {
		var remainingArgs = Args.parseOrExit(Compiler.class, args);

		if (remainingArgs.isEmpty()) {
			System.out.println("Usage: tc filename [-objectName (-o) outputfilename] [-showTree] [-folding] [-showTreeAfter] [-hoisting] [-showStats]");
			System.exit(1);
		}

		return remainingArgs.get(0);
	}
}
