/*
 * @(#)Compiler.java                       
 * 
 * Revisions and updates (c) 2022-2025 Sandy Brownlee. alexander.brownlee@stir.ac.uk
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
import triangle.optimiser.StatsVisitor;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

import java.util.List;
import java.util.ArrayList;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

/**
 * The main driver class for the Triangle compiler.
 */
public class Compiler {

	/** The filename for the object program, normally obj.tam. */
	@Argument(description = "Name of output object file", alias = "o")
	static String objectName = "obj.tam";
	
	@Argument(description = "Show AST after contextual analysis", alias = "t")
	static Boolean showTree = false;
	@Argument(description = "Enable constant folding", alias = "f")
	static Boolean folding = false;
	@Argument(description = "Show AST after constant folding", alias = "a")
	static Boolean showTreeAfter = false;
	@Argument(description = "Show AST statistics (IntegerExpressions, CharacterExpressions)", alias = "s")
	static Boolean showStats = false;
	@Argument(description = "Enable hoisting (loop-invariant code motion) after folding", alias = "h")
	static Boolean hoisting = false;

	// CLI managed via cli-parser; annotated fields are parsed via Args.parseOrExit.
	
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
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass
			if (showingAST) {
				drawer.draw(theAST);
			}

			// Optional: statistics (Task 6)
			if (showStats) {
				StatsVisitor stats = new StatsVisitor();
				theAST.visit(stats); // walk full AST
				System.out.println("Stats: IntegerExpressions=" + stats.getIntegerCount() + ", CharacterExpressions=" + stats.getCharacterCount());
			}

			// Optional: constant folding (Task 7)
			if (folding) {
				theAST.visit(new ConstantFolder());
				if (showTreeAfter) {
					drawer.draw(theAST);
				}
			}

			// Optional: hoisting (Task 8) - after constant folding
			if (hoisting) {
				if (theAST != null && theAST.C != null) {
					theAST.C = Hoister.transform(theAST.C);
				}
			}
				
			// Re-run contextual analysis to ensure types/decls are set after AST transforms
			checker.check(theAST);
			
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
		
		// Normalize GNU-style --options to cli-parser -options and split --key=value into tokens
		String[] norm = normalizeArgs(args);

		// Populate annotated static fields; returns remaining non-option args (source filename)
		List<String> rest = Args.parseOrExit(Compiler.class, norm);

		if (rest.size() < 1) {
			System.out.println("Usage: tc <filename> [-objectName obj.tam] [-showTree] [-folding] [-showTreeAfter] [-showStats] [-hoisting]");
			System.exit(1);
		}

		String sourceName = rest.get(0);

		boolean compiledOK = compileProgram(sourceName, objectName, showTree, false);

		// Keep process alive if any tree window is requested
		if (!showTree && !showTreeAfter) {
			System.exit(compiledOK ? 0 : 1);
		}
	}

	/**
	 * Convert --long and --key=value to -long and separate tokens for cli-parser.
	 */
	private static String[] normalizeArgs(String[] args) {
		List<String> out = new ArrayList<>(args.length);
		for (String a : args) {
			if (a.startsWith("--")) {
				String s = a.substring(2);
				int eq = s.indexOf('=');
				if (eq >= 0) {
					String key = s.substring(0, eq);
					String val = s.substring(eq + 1);
					out.add("-" + key);
					if (!val.isEmpty()) out.add(val);
				} else {
					out.add("-" + s);
				}
			} else if (a.startsWith("-") && a.contains("=")) {
				int eq = a.indexOf('=');
				String key = a.substring(1, eq);
				String val = a.substring(eq + 1);
				out.add("-" + key);
				if (!val.isEmpty()) out.add(val);
			} else {
				out.add(a);
			}
		}
		return out.toArray(new String[0]);
	}
}
