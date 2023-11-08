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

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.SummaryStatistics;
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

	/** The filename for the object program, normally obj.tam. */
	public static Scanner scanner;
	public static Parser parser;
	public static Checker checker;
	public static Encoder encoder;
	public static Emitter emitter;
	public static ErrorReporter reporter;
	public static Drawer drawer;

	/** The AST representing the source program. */
	public static Program theAST;
	public static Drawer foldedDrawer;
	public static SummaryStatistics summarizer;


	//for the compiler default is false

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
		foldedDrawer = new Drawer();
		summarizer = new SummaryStatistics();
		//scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {

			System.out.println("Contextual Analysis ...");
			 // 2nd pass
			if(showSummaryStats){
				theAST.visit(summarizer);
			}
			checker.check(theAST);
			if (showingAST) {
				drawer.draw(theAST);
			}
			if (folding) {
				theAST.visit(new ConstantFolder());
				checker.check(theAST);
				if (showTreeAfterFolding) {
					foldedDrawer.draw(theAST);
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
			System.out.println("Compilation was successful.");
			if(showSummaryStats){
				System.out.println("number of character expressions");
				System.out.println(summarizer.numberOfCharacterExpressions);
				System.out.println("number of Integer expressions");
				System.out.println(summarizer.numberOfIntegerExpressions);
			}
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
@Argument(alias="s", description = "Source Name Path")
public static String sourceNamePath = "programs/loopwhile.tri";
@Argument(alias="o",description = "Object Name")
public static String objectSourceName = "loopwhile.tam";

//the declared booleans below were kept here for dev purposes allowing for one big block of code that can be manipulated
	// it was also decided to keep them like this in order to have a default value instead of requiring input
	// this means that they do not have to be parameters of the methods which follows Clean Code Principles
public static boolean showTree = true;

public static boolean folding = false;
public static boolean showTreeAfterFolding = false;
public static boolean showSummaryStats = true;
	public static void main(String[] args) {
		//these lines have been commented out to allow for the cliparser to manipulate the args
		/**
		 if (args.length < 1) {
			System.out.println("Usage: tc filename [-o=outputfilename] [tree] [folding]");
			System.exit(1);
		}
		parseArgs(args);
		sourceNamePath = args[0];
		 */
		Compiler compiler = new Compiler();
		Args.parseOrExit(compiler,args);
		var compiledOK = compileProgram(sourceNamePath, objectSourceName,showTree,false);
		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}

//parseArgs has cases added for when cliparser is no longer used
	private static void parseArgs(String[] args) {
	for (String s : args) {
		var sl = s.toLowerCase();
		if (sl.equals("tree")) {
			showTree = true;
		} else if (sl.startsWith("-o=")) {
			objectSourceName = s.substring(3);
		} else if (sl.equals("folding")) {
			folding = true;
		}else if (sl.equals("stats")) {
			showSummaryStats = true;
		}else if (sl.equals("treeafterfolding")) {
			showTreeAfterFolding = true;
		}
	}
}
}