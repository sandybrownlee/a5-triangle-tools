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


/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 */
public class Compiler {

	@Argument(alias = "o", description = "Name of the output object file", required = false)
	private static String objectName = "obj.tam";

	@Argument(alias = "t", description = "Display the Abstract Syntax Tree (AST)", required = false)
	private static boolean showTree = false;

	@Argument(alias = "f", description = "Enable constant folding", required = false)
	private static boolean folding = false;

	@Argument(alias = "a", description = "Display AST after folding", required = false)
	private static boolean showTreeAfter = false;

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
	 * @param showingASTAfter true iff the AST is to be displayed after folding
	 * @return true iff the source program is free of compile-time errors, otherwise false.
	 */
	static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingASTAfter) {

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
			if (showingASTAfter) {
				drawer.draw(theAST);
			}
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, false); // 3rd pass
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
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		try {
			// this will parse the list of arguments passed into the program, and
			// populate the appropriate instance variables
			// if the required arguments were not found, it will gracefully exit
			Args.parseOrExit(Compiler.class, args);

			System.out.println("Arguments parsed:");
			System.out.println("objectName = " + objectName);
			System.out.println("showTree = " + showTree);
			System.out.println("folding = " + folding);
			System.out.println("showTreeAfter = " + showTreeAfter);

			String sourceName = args[0];
			boolean compiledOK = compileProgram(sourceName, objectName, showTree, showTreeAfter);

			System.exit(compiledOK ? 0 : 1);
		} catch (Exception e) {
			System.err.println("Error parsing arguments: " + e.getMessage());
			System.exit(1);
		}
	}
}
