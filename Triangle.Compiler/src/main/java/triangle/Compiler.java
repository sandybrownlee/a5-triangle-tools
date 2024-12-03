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

import org.apache.commons.cli.*;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.visitors.SummaryVisitor;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.HoistingVisitor;
import triangle.optimiser.InvariantIdentifierVisitor;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.contextualAnalyzer.Checker;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.treeDrawer.Drawer;
import java.util.Set;
import java.util.HashSet;

public class Compiler {

    static String objectName = "obj.tam";
    static boolean showTree = false;
    static boolean folding = false;
    static boolean showTreeAfter = false;
    static boolean showStats = false;
    static boolean hoistingEnabled = false;

    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;

    private static Program theAST;

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
                theAST.visit(new ConstantFolder(), null);
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

        // If showStats option is enabled, run the SummaryVisitor
        if (showStats) {
            SummaryVisitor summaryVisitor = new SummaryVisitor();
            theAST.visit(summaryVisitor, null);

            System.out.println("Binary Expressions: " + summaryVisitor.getBinaryExpressionCount());
            System.out.println("If Commands: " + summaryVisitor.getIfCommandCount());
            System.out.println("While Commands: " + summaryVisitor.getWhileCommandCount());
        }

        // If showTreeAfter option is enabled, display the tree after folding
        if (showTreeAfter) {
            drawer.draw(theAST);
        }

        return successful;
    }

    public static void main(String[] args) {

        Options options = new Options();

        Option objectNameOption = new Option("o", "objectName", true, "Object name");
        objectNameOption.setRequired(true);
        options.addOption(objectNameOption);

        Option showTreeOption = new Option("s", "showTree", false, "Show tree");
        options.addOption(showTreeOption);

        Option foldingOption = new Option("f", "folding", false, "Folding");
        options.addOption(foldingOption);

        Option showTreeAfterOption = new Option("a", "showTreeAfter", false, "Show tree after folding");
        options.addOption(showTreeAfterOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String sourceName = cmd.getArgs().length > 0 ? cmd.getArgs()[0] : null;
        if (sourceName == null) {
            System.out.println("Source file is required.");
            System.exit(1);
        }

        objectName = cmd.getOptionValue("objectName", "obj.tam");
        showTree = cmd.hasOption("showTree");
        folding = cmd.hasOption("folding");
        showTreeAfter = cmd.hasOption("showTreeAfter");

        // Initialize the compilation process
        var compiledOK = compileProgram(sourceName, objectName, showTree, false);

        // Perform constant folding optimization if enabled
        if (folding) {
            ConstantFolder constantFolder = new ConstantFolder();
            theAST.visit(constantFolder, null);
        }

        // Perform hoisting optimization if enabled
        if (hoistingEnabled) { 
            Set<String> updatedVariables = new HashSet<>(); 
            theAST.visit(new InvariantIdentifierVisitor(), updatedVariables); 
            theAST.visit(new HoistingVisitor(updatedVariables), null);
        }
        
        if (!showTree) {
            System.exit(compiledOK ? 0 : 1);
        }
    }
}
