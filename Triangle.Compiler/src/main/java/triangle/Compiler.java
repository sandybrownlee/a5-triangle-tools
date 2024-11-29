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

	/** The name of the file containing the source program.  */
	@Argument(alias="s", required=true, description="name of file containing source program")
	protected static String sourceName;

	/** The name for the compiled object program, defaults to "obj.tam". */
	@Argument(alias="o", required=false, description="the compiled Triangle program's name")
	protected static String objectName = "obj.tam";

	/** Enable constant folding for constant values. */
	@Argument(alias="f", required=false, description="fold constant values when compiling")
	protected static boolean folding;

	/** Display the abstract syntax tree. */
	@Argument(alias="sT", required=false, description="display the program's abstract syntax tree")
	protected static boolean showTree;

	/** Display the abstract syntax tree after applying constant folding. */
	@Argument(alias="fsT", required=false, description="display the abstract syntax tree after applying constant folding")
	protected static boolean showTreeAfter;

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program ast;



	/**
	 * Triangle compiler main program.
	 *
	 * @param args the only command-line argument to the program specifies the
	 *             source filename.
	 */
	public static void main(String[] args) {

		// parse each argument - exiting on error.
		Args.parseOrExit(new Compiler(), args);

		// compile and store result.
		boolean compiledOK = compileProgram(
				sourceName,
				objectName,
				folding,
				showTree,
				showTreeAfter,
				false
		);

		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}





	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName  the name of the file containing the source program.
	 * @param objectName  the name of the file containing the object program.
	 * @param folding 	  fold constant values when compiling program.
	 * @param showAST     true if the AST is to be displayed after contextual
	 *                    analysis
	 * @param showTable   true if the object description details are to be
	 *                    displayed during code generation (not currently
	 *                    implemented).
	 * @return true if the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(
			String sourceName,
			String objectName,
			boolean folding,
			boolean showAST,
			boolean showTreeAfter,
			boolean showTable
	) {
		System.out.printf("********** Triangle Compiler (Java Version %s) **********\n",
				System.getProperty("java.version"));

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
		ast = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			System.out.println("Contextual Analysis ...");
			checker.check(ast); // 2nd pass
			if (showAST) {
				drawer.draw(ast);
			}
			if (folding) {
				ast.visit(new ConstantFolder());
			}
			// show difference in AST after constant folding.
			// print message if program compiled without folding.
			if (showTreeAfter) {

				if (folding) {
					drawer.draw(ast);
				}
				else {
					System.out.println("No folded AST to display, program was compiled without constant folding");
				}
			}
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(ast, showTable); // 3rd pass
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



	@Deprecated
	private static void parseArgs(String[] args) {
		for (String s : args) {
			var sl = s.toLowerCase();
			if (sl.equals("tree")) {
				showTree = true;
			} else if (sl.startsWith("-o=")) {
				objectName = s.substring(3);
			} else if (sl.equals("folding")) {
				folding = true;
			}
		}
	}
}
