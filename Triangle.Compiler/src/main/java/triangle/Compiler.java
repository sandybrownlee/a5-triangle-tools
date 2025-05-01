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
import com.sampullara.cli.Argument;  //cli imports
import com.sampullara.cli.Args;  //cliparser imports 
import java.util.List;
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

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {
	/** 
     * Name of the output object file 
     * Can be set with -o; defaults to "obj.tam" 
     */
	@Argument(alias = "o", description = "Name of object file (default obj.tam)")
    static String objectName = "obj.tam";
	 /** 
     * If true, draw the AST after parsing and contextual analysis 
     */
    @Argument(description = "Show AST before constant folding")
    static boolean showTree = false;
    /** 
     * If true, perform the constant-folding optimization pass 
     */
    @Argument(description = "Perform constant folding optimisations")
    static boolean folding = false;
    /** 
     * If true, draw the AST again after constant folding is complete 
     */
    @Argument(description = "Show AST after constant folding is complete")
    static boolean showTreeAfter = false;
    /** 
     * If true, print summary stats (#BinaryExpressions, #IfCommands, #WhileCommands) 
     */
    @Argument(description = "Show summary statistics (number of BinaryExpressions, IfCommands, WhileCommands)")
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
			if (showTreeAfter) {
                drawer.draw(theAST);
                if (showStats) {
                    System.out.println("Summary Statistics (before code generation):");
                    SummaryVisitor visitor = new SummaryVisitor();
                    theAST.visit(visitor, null);
                    System.out.println("BinaryExpressions: " + visitor.getBinaryCount());
                    System.out.println("IfCommands: "          + visitor.getIfCount());
                    System.out.println("WhileCommands: "       + visitor.getWhileCount());
                }
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
			}
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
			System.out.println("Usage: tc filename [-o=outputfilename] [tree] [folding]");
			System.exit(1);
		}
		
		// Parse annotations on static fields and populate options
        List<String> unparsed = Args.parseOrExit(Compiler.class, args);  
        if (unparsed.isEmpty()) {
            System.out.println("Error: No source filename provided.");
            System.exit(1);
        }
        String sourceName = unparsed.get(0);

        boolean compiledOK = compileProgram(sourceName, objectName, showTree, false);
        System.exit((!showTree && !showTreeAfter) ? (compiledOK ? 0 : 1) : 0);
    }
}
	