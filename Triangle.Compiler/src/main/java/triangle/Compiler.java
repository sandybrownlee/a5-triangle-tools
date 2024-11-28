package triangle;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import triangle.abstractSyntaxTrees.visitors.SummaryVisitor;

/**
 * The main driver class for the Triangle compiler.
 *
 * Original release:
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 */
public class Compiler {

    // Argument to specify output file
    @Argument(alias = "o", description = "Name of the output object file", required = false)
    static String objectName = "obj.tam";

    // Argument to show tree structure
    @Argument(alias = "s", description = "Show tree structure", required = false)
    static boolean showTree = false;

    // Argument to enable constant folding
    @Argument(alias = "f", description = "Enable folding", required = false)
    static boolean folding = false;

    // Argument to show tree after folding
    @Argument(alias = "t", description = "Show the tree after folding", required = false)
    static boolean showTreeAfter = false;

    //argument to show program statistcs
    @Argument(alias = "ps", description = "Show program statistics", required = false)
    static boolean showStats = false;

    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;
    private static Program theAST;

    /**
     * Compile the source program to TAM machine code.
     */
    static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable, boolean showStats) {
        System.out.println("********** Triangle Compiler (Java Version 2.1) **********");

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

        // Parsing (1st pass)
        theAST = parser.parseProgram();
        if (reporter.getNumErrors() == 0) {
            System.out.println("Contextual Analysis ...");
            checker.check(theAST); // 2nd pass
            if (showingAST) {
                drawer.draw(theAST);
            }
            if (folding) {
                theAST.visit(new ConstantFolder()); // Optimize AST
                if (showTreeAfter) {
                    drawer.draw(theAST);
                }
            }
            if(showStats){
                SummaryVisitor sv = new SummaryVisitor();
                sv.visitProgram(theAST, null); // Generate summary statistics.
                sv.printSummary();


            }


            if (reporter.getNumErrors() == 0) {
                System.out.println("Code Generation ...");
                encoder.encodeRun(theAST, showingTable); // 3rd pass
            }
        }

        boolean successful = reporter.getNumErrors() == 0;
        if (successful) {
            emitter.saveObjectProgram(objectName);
            System.out.println("Compilation was successful.");
        } else {
            System.out.println("Compilation was unsuccessful.");
        }

        return successful;
    }

    /**
     * Main method for the compiler.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: tc filename [-o outputFileName] [-s] [-f] [-t]");
            System.exit(1);
        }

        Args.parseOrExit(new Compiler(), args);

        String sourceName = args[0];
        var compiledOK = compileProgram(sourceName, objectName, showTree, false, showStats);

        if (!showTree && !showTreeAfter) {
            System.exit(compiledOK ? 0 : 1);

        }
    }
}
