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

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.parser.errors.ParseException;

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
	private String objectName = "obj.tam";
	
	private boolean showTree = false;
	private boolean folding = false;
	private boolean showTreeAfter = false;

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
	 * @return true if the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	private boolean compileProgram(String sourceName) {

		System.out.println("********** " + "Triangle Compiler" + " **********");

		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceName);

		if (source == null) {
			System.out.println("Can't access source file " + sourceName);
			return false;
		}

		scanner = new Scanner(source);
		reporter = new ErrorReporter(false);
		parser = new Parser(scanner, reporter);
		checker = new Checker(reporter);
		emitter = new Emitter(reporter);
		encoder = new Encoder(emitter, reporter);
		drawer = new Drawer();

        theAST = parser.parseProgram();
        if (reporter.getNumErrors() > 0) {
            return false;
        }

        System.out.println("Contextual Analysis ...");
        checker.check(theAST);
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
            encoder.encodeRun(theAST, false);
        }

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else
			System.out.println("Compilation was unsuccessful.");
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
            Compiler compiler = SingleCommand.singleCommand(Compiler.class).parse(args);

            if (args.length == 0) {
                System.out.println("Usage: java -jar TriangleCompiler.jar [options] <sourcefile>");
                System.exit(1);
            }

            String sourceName = args[args.length - 1];
            boolean success = compiler.compileProgram(sourceName);
            System.exit(success ? 0 : 1);

        } catch (ParseException e) {
            System.err.println("Error parsing command-line arguments: " + e.getMessage());
            System.exit(1);
        }
	}
}
