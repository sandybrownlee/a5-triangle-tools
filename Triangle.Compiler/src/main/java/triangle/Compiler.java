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
import com.github.rvesse.airline.parser.Cli;
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import triangle.visitors.SummaryVisitor; // Import the SummaryVisitor class

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 */
@Command(name = "triangle", description = "Triangle Compiler Options")
public class Compiler {

    @Option(name = "--objectName", description = "Name of the object file")
    private static String objectName = "obj.tam";

    @Option(name = "--showTree", description = "Display the abstract syntax tree")
    private static boolean showTree = false;

    @Option(name = "--folding", description = "Enable constant folding")
    private static boolean folding = false;

    @Option(name = "--showTreeAfter", description = "Display the tree after folding")
    private static boolean showTreeAfter = false;

    @Option(name = "--showStats", description = "Display program summary statistics (BinaryExpressions, IfCommands, WhileCommands)")
    private static boolean showStats = false;

    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;

    private static Program theAST;

    public static void main(String[] args) {
        // Use cli-parser to parse arguments
        Cli<Compiler> cli = new Cli<>(Compiler.class);
        try {
            cli.parse(args);
        } catch (Exception e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            System.exit(1);
        }

        if (args.length < 1) {
            System.out.println("Usage: tc filename [--objectName=outputfilename] [--showTree] [--folding] [--showTreeAfter] [--showStats]");
            System.exit(1);
        }

        String sourceName = args[0];

        boolean compiledOK = compileProgram(sourceName, objectName, showTree, false);

        // If showStats option is enabled, apply SummaryVisitor to the AST
        if (showStats) {
            SummaryVisitor summaryVisitor = new SummaryVisitor();
            theAST.visit(summaryVisitor);
            System.out.println("Binary Expressions: " + summaryVisitor.getBinaryExpressionCount());
            System.out.println("If Commands: " + summaryVisitor.getIfCommandCount());
            System.out.println("While Commands: " + summaryVisitor.getWhileCommandCount());
        }

        if (showTreeAfter && folding) {
            System.out.println("Displaying the Abstract Syntax Tree after Folding...");
            drawer.draw(theAST);
        }

        System.exit(compiledOK ? 0 : 1);
    }

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

        theAST = parser.parseProgram(); // 1st pass
        if (reporter.getNumErrors() == 0) {
            System.out.println("Contextual Analysis ...");
            checker.check(theAST); // 2nd pass
            if (showingAST) {
                drawer.draw(theAST);
            }
            if (folding) {
                theAST.visit(new ConstantFolder());
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
}
