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

import triangle.abstractSyntaxTrees.*;
import triangle.visitors.Visitor;
import triangle.visitors.PrettyPrintVisitor;
import triangle.visitors.TypeDepthVisitor; //all above newly imported - remember in report dumbass
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import com.sampullara.cli.Argument;
import com.sampullara.cli.Args;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class StatisticsVisitor implements Visitor<Void> {
	private int characterExpressionCount = 0;
	private int integerExpressionCount = 0;

	@Override
	public Void visitCharacterExpression(CharacterExpression ast) {
		characterExpressionCount++;
		return null;
	}

	@Override
	public Void visitIntegerExpression(IntegerExpression ast) {
		integerExpressionCount++;
		return null;
	}

	public void printStatistics() {
		System.out.println("Character Expressions: " + characterExpressionCount);
		System.out.println("Integer Expressions: " + integerExpressionCount);
	}
}

public class Compiler {

	/** The filename for the object program, normally obj.tam. */
	@argument(alias = "o", description = "show the object program filename")
	private String objectName = "obj.tam";

	@argument(alias = "tree", description = "Show the tree after contextual analysis")
	private boolean showTree = false;

	@argument(alias = "folding", description = "Enable constant folding during code generation")
	private boolean folding = false;

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
		drawer = new Drawer();

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass

		if (reporter.getNumErrors() == 0) {
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass
			if (showingAST) {
				drawer.draw(theAST);
			}

			if (compiler.folding) {
				theAST.visit(new ConstantFolder());
			}

			if (hoisting) { // Apply hoisting optimization
				System.out.println("Hoisting Optimization ...");
				HoistingOptimizer hoistingOptimizer = new HoistingOptimizer();
				theAST = theAST.visit(hoistingOptimizer, null);
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

	/**
	 * Triangle compiler main program.
	 *
	 * @param args the only command-line argument to the program specifies the
	 *             source filename.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java Compiler [-o=outputfilename] [--tree] [--folding] filename");
			System.exit(1);
		}

		Compiler compiler = new Compiler();

		for (String arg : args) {
			var sl = arg.toLowerCase();
			if (sl.equals("--tree")) {
				compiler.showTree = true; // Set showTree flag
			} else if (sl.equals("--folding")) {
				compiler.folding = true; // Set folding flag
			}
		}

		String sourceName = args[args.length - 1];
		var compiledOK = compileProgram(sourceName, compiler.objectName, compiler.showTree, compiler.folding);

		if (!compiler.showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}

	private static void parseArgs(String[] args) {
		Compiler compiler = new Compiler(); // Create an instance of the Compiler class
		for (String s : args) {
			var sl = s.toLowerCase();
			if (sl.equals("tree")) {
				compiler.showTree = true; // Update showTree in the compiler instance
			} else if (sl.startsWith("-o=")) {
				compiler.objectName = s.substring(3); // Update objectName in the compiler instance
			} else if (sl.equals("folding")) {
				compiler.folding = true; // Update folding in the compiler instance
			}
		}
	}
}
