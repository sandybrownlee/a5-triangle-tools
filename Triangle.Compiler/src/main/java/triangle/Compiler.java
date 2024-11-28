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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//import main.java.triangle.abstractSyntaxTrees.visitors.SummaryVisitor;
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

	/** The filename for the object program, normally obj.tam. */

	static String objectName = "obj.tam";
	static boolean showTree = false;
	static boolean folding = false;
	static boolean showTreeAfter = false;
	static boolean showStats = false;




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
			if(showStats){
				//SummaryVisitor summaryVisitor = new SummaryVisitor();
                //theAST.visit(summaryVisitor, null); // visits the summaryVisitor class
                //System.out.println(summaryVisitor.printSummary()); //displays the string format of the summary stats
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

		//if no arguments presented, displays the usage of the parser.
		if (args.length < 1) {
            System.out.println("Usage: tc filename [-o outputfilename] [-t] [-f] [-a] [-s]");
            System.exit(1);
        }

		//initialising the options object
		Options options = new Options();
		//option builder to create the object Name option
		options.addOption(Option.builder("o")
		.hasArg(true)
		.argName("objectName")
		.desc("Name of the object being compiled")
		.build());
		
		//creating the options for the cli parser
		options.addOption(new Option("t","showTree",false, "Show the syntax tree"));
		options.addOption(new Option("f","folding",false, "Enable or disable folding"));
		options.addOption(new Option("a","showTreeAfter",false, "Show the tree after folding"));
		options.addOption(new Option("s","showStats",false, "Show the number of visits to ASTs"));

		CommandLineParser parser = new DefaultParser();
		
		try{
			CommandLine cmd = parser.parse(options,args);
			if(cmd.hasOption("o")){					//checks if a name value was provided, if so objectName is set to the file name
				objectName=cmd.getOptionValue("o");
			}
			//checks if the option values are provided, if not they remain disabled
			showTree = cmd.hasOption("t");
			folding = cmd.hasOption("f");
			showTreeAfter=cmd.hasOption("a");
			showStats=cmd.hasOption("s");

			String sourceName=cmd.getArgs()[0];
			boolean success= compileProgram(sourceName, objectName, showTree, showTreeAfter);

		}
		catch(ParseException e){
			System.out.println("Command line parsing failed: " + e.getMessage());
			System.exit(1);
		}
			
	
	}
	
}
