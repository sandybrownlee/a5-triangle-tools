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
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import triangle.optimiser.StatisticsVisitor;

/**
 * The main driver class for the Triangle compiler.
 */
public class Compiler {

    @Parameters(index = "0", description = "The source file to compile.")
    static String sourceName;
    /**
     * The filename for the object program, normally obj.tam.
     */
    @Option(names = "-o", description = "The output object file name. Default: ${DEFAULT-VALUE}")
    static String objectName = "obj.tam";

    @Option(names = {"-t", "--show-tree"}, description = "Show the AST before folding.")
    static boolean showTree = false;

    @Option(names = {"-f", "--folding"}, description = "Enable constant folding.")
    static boolean folding = false;

    @Option(names = {"-a", "--show-tree-after"}, description = "Show the AST after folding.")
    static boolean showTreeAfter = false;

    @Option(names = {"-s", "--show-stats"}, description = "Show program statistics (Char/Integer Expression counts).")
    static boolean showStats = false;

    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;

    /**
     * The AST representing the source program.
     */
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
     * false.
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

            if (showStats) {
                StatisticsVisitor stats = new StatisticsVisitor();
                theAST.visit(stats, null);

                System.out.println("--- Program Statistics ---");
                System.out.println("Integer Expressions: " + stats.getIntegerExpressionCount());
                System.out.println("Character Expressions: " + stats.getCharacterExpressionCount());
                System.out.println("--------------------------");
            }

            System.out.println("Contextual Analysis ...");
            checker.check(theAST); // 2nd pass
            if (showingAST) {
                System.out.println("--- AST Before Folding ---");
                drawer.draw(theAST);
            }
            if (folding) {
                theAST.visit(new ConstantFolder());
            }

            if (showTreeAfter) {
                System.out.println("--- AST After Folding ---");
                drawer.draw(theAST);
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
     * @param args the command-line arguments.
     */
    public static void main(String[] args) {

        // Use picocli to parse the arguments and populate the static fields
        new CommandLine(Compiler.class).parseArgs(args);

        // Check if the required sourceName parameter was provided
        if (sourceName == null) {
            System.err.println("Usage: tc <source file> [options]");
            // Print the full help menu
            new CommandLine(Compiler.class).usage(System.out);
            System.exit(1);
        }

        // Call compileProgram with the populated static fields
        // sourceName (from @Parameters) -> sourceName
        // objectName (from @Option)     -> objectName
        // showTree (from @Option)       -> showingAST
        var compiledOK = compileProgram(sourceName, objectName, showTree, false);

        // Don't exit immediately if we are showing a tree,
        // so the user has time to see it.
        if (!showTree && !showTreeAfter) {
            System.exit(compiledOK ? 0 : 1);
        }
    }
}
