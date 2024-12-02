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
import triangle.abstractSyntaxTrees.visitors.SummaryVisitors;
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

import java.util.List;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

	//Arguments for when compiling
	@Argument(value = "sourceName", description = "Source of the object to be compiled")
	private static String sourceName;
	@Argument(value = "objectName", description = "Name of the object to be compiled")
	private static String objectName; //name of the object to be compiled, obj.tam being default
	@Argument(value = "showStats", description = "Show the number of binary, ifs and while commands")
	private static boolean showStats = false; //name of the object to be compiled, obj.tam being default
	@Argument(value = "showTree", description = "Display the abstract syntax tree")
	private static boolean showTree = false; //is abstract syntax tree showing when compiling
	@Argument(value = "folding", description = "Enable folding")
	private static boolean folding = false; //is folding features when compiling
	@Argument(value = "showTreeAfter", description = "Display the tree after folding is complete")
	private static boolean showTreeAfter = false; //display the tree

	/** The filename for the object program, normally obj.tam. */
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
			if (folding) {
				theAST.visit(new ConstantFolder());
			}

			//display the stats
			if(showStats){
				System.out.println("Generating program statistics...");
				SummaryVisitors visitor = new SummaryVisitors();
				theAST.visit(visitor, null); // Traverse the AST with the SummaryVisitors
				visitor.getSummary(); // Print the summary
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
		try {
			//Process command-line arguments
			Args.parseOrExit(Compiler.class, args);
//			List<String> positionalArgs = Args.parseOrExit(Compiler.class, args);
//			String sourceName = positionalArgs.get(0);
			var compiledOK = compileProgram(sourceName, objectName, showTree, false);
			if (!showTree) {
				System.exit(compiledOK ? 0 : 1);
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Error parsing arguments: " + e.getMessage());
			Args.usage(Compiler.class);
			System.exit(1);
		}

		//Run the compiler logic
		run();
	}

	//test function for when running the program
	private static void run() {
		// Print out the parsed arguments to verify
		System.out.println("Object Name: " + objectName);
		System.out.println("Show Tree: " + showTree);
		System.out.println("Folding: " + folding);
		System.out.println("Show Tree After Folding: " + showTreeAfter);
		// Proceed with the compiler logic using the parsed arguments
	}
}
