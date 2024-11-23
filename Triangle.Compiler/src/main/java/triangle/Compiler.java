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
import triangle.optimiser.SummaryVisitor;
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

	@Argument(alias = "sf", description = "source of file", required = true)
	 private static String sourceFile;
	/** The filename for the object program, normally obj.tam. */
	@Argument(alias = "o", description = "name of the object to be output", required = false)
	private static String objectName = "obj.tam";
	@Argument(alias = "st", description = "show AST representation after compilation?", required = false)
	private static boolean showTree = false;
	@Argument(alias = "fo", description = "enable folding optimisations?", required = false)
	private static boolean folding = false;
	@Argument(alias = "ta", description = "show tree after tree optimisations?", required = false)
	private static boolean showTreeAfter = false;
	@Argument(alias = "stats", description = "show tree after tree optimisations?", required = false)
	private static boolean showStatistics = false;

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;
	private static SummaryVisitor summaryVisitor;

	/** The AST representing the source program. */
	private static Program theAST;

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @var showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram() {

		System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");
		System.out.println("Compiling program: " + sourceFile + "...");
		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceFile);

		if (source == null) {
			System.out.println("Can't access source file " + sourceFile);
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
					showTree = true; // I forgot that checking !showTree after for system.exit is comparing true || false, therefore exiting when trying to draw folded tree
					drawer.draw(theAST);
				}
			}
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, false); // 3rd pass
			}
			if (showStatistics) {
				SummaryVisitor summaryVisitor = new SummaryVisitor();
				summaryVisitor.countStats(theAST);
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
		Args.parseOrExit(Compiler.class, args);

		var compiledOK = compileProgram();

		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}
}
