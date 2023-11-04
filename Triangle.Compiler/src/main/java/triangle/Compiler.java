package triangle;
import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.ExpressionCounter;
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
    static String objectName = "obj.tam";
    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;
    private static ExpressionCounter summary;

    @Argument(value = "output", description = "name for the output file")
    static String outputFileName;

    @Argument(value = "displayAST", description = "Displays the Abstract syntax tree")
    static boolean displayAST;

    @Argument(value = "enableFold", description = "Enables Folding")
    static boolean enableFolding;

    @Argument(value = "showAfterFold", description = "Show Abstract syntax tree after folding")
    static boolean showASTAfterFolding;

    @Argument(value = "showStats", description = "Show total CharacterExpressions and IntegerExpressions")
    static boolean showStats;


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
static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable, boolean folding,
                              boolean showASTAfterFolding, boolean showSummaryStatistics) {

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
    summary = new ExpressionCounter();

    theAST = parser.parseProgram(); // 1st pass
    if (reporter.getNumErrors() == 0) {
        if (showingAST) {			drawer.draw(theAST);
        }
        System.out.println("Contextual Analysis ...");
        checker.check(theAST); // 2nd pass
        if (showingAST) {
            System.out.println("AST before Folding:");
            drawer.draw(theAST);
        }
        if (folding) {
            theAST.visit(new ConstantFolder());			System.out.println("Folding Enabled ...");
            checker.check(theAST);
            if (showASTAfterFolding) {
                System.out.println("AST after Folding:");
                drawer.draw(theAST);
            }
        } else {
            System.out.println("Folding not enabled ...");
        }


        if (reporter.getNumErrors() == 0) {
            System.out.println("Code Generation ...");
            encoder.encodeRun(theAST, showingTable); // 3rd pass
        }

        if (showSummaryStatistics) {
            System.out.println("Summary Statistics:");
            summary.countExpressions(theAST);
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

            Compiler compiler = new Compiler();
            Args.parseOrExit(compiler, args);

            String sourceName = args[0];
            var compiledOK = compileProgram(sourceName, outputFileName, displayAST, false, enableFolding, showASTAfterFolding, showStats);

            if (!displayAST) {
                System.exit(compiledOK ? 0 : 1);
            }


        }

    }