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

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Lexer;
import triangle.syntacticAnalyzer.SyntaxError;
import triangle.treeDrawer.Drawer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 The main driver class for the Triangle compiler.

 @author Deryck F. Brown
 @version 2.1 7 Oct 2003 */
public class Compiler {

    /** The filename for the object program, normally obj.tam. */
    @Argument(description = "The name of the file to store the object code into")
    static String objectName = "obj.tam";

    @Argument(description = "The name of the source file to compile", required = true)
    private static String sourceName;

    @Argument(description = "Whether or not to show the tree")
    private static boolean showTree;

    @Argument(description = "Whether or not to turn on constant folding")
    private static boolean folding;

    @Argument(description = "Whether or not to show the tree after folding")
    private static boolean showTreeAfterFolding;

    /**
     Triangle compiler main program.

     @param args the only command-line argument to the program specifies the
     source filename.
     */
    public static void main(String[] args) {
        Args.parseOrExit(Compiler.class, args);

        boolean compiledOK = false;
        try {
            compiledOK = compileProgram(new FileInputStream(sourceName), objectName, showTree, false);
        } catch (FileNotFoundException e) {
            System.err.println("Could not open file: " + sourceName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IOException while compiling");
            e.printStackTrace();
            System.exit(1);
        } catch (SyntaxError e) {
            System.err.println("SyntaxError while compiling");
            e.printStackTrace();
            System.exit(1);
        }

        if (!showTree) {
            System.exit(compiledOK ? 0 : 1);
        }
    }

    /**
     Compile the source program to TAM machine code.

     @param inputStream   an {@link InputStream} from which to read the source code
     @param objectName   the name of the file containing the object program.
     @param showingAST   true iff the AST is to be displayed after contextual
     analysis
     @param showingTable true iff the object description details are to be
     displayed during code generation (not currently
     implemented).

     @return true iff the source program is free of compile-time errors, otherwise
     false.
     */
    static boolean compileProgram(InputStream inputStream, String objectName, boolean showingAST, boolean showingTable)
            throws IOException, SyntaxError {

        System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

        System.out.println("Syntactic Analysis ...");

        Lexer lexer = new Lexer(inputStream);
        ErrorReporter reporter = new ErrorReporter(false);
        Parser parser = new Parser(lexer, reporter);
        Checker checker = new Checker(reporter);
        Emitter emitter = new Emitter(reporter);
        Encoder encoder = new Encoder(emitter, reporter);
        Drawer drawer = new Drawer();

        // lexer.enableDebugging();

        // The AST representing the source program.
        Program theAST = parser.parseProgram(); // 1st pass
        if (reporter.getNumErrors() == 0) {

            // TODO:
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
