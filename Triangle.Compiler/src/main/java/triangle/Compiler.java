/*
 * @(#)Compiler.java
 *
 * Revisions and updates (c) 2024 Zaraksh Rahman. zar00024@students.stir.ac.uk
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
import triangle.analysis.Desugarer;
import triangle.analysis.SemanticAnalyzer;
import triangle.analysis.TypeChecker;
import triangle.codegen.ObjectWriter;
import triangle.codegen.CodeGen;
import triangle.repr.Instruction;
import triangle.codegen.Optimizer;
import triangle.parsing.Parser;
import triangle.parsing.SyntaxError;
import triangle.repr.Statement;
import triangle.util.ASTPrinter;
import triangle.util.SummaryVisitor;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Compiler {

    @Argument(description = "The name of the file to store the object code into") private static String objectName = "obj.tam";

    @Argument(description = "The name of the source file to compile", required = true) private static String sourceName;

    @Argument(description = "Turn on constant folding") private static boolean constantFolding;

    @Argument(description = "Turn on loop hoisting") private static boolean hoisting;

    @Argument(description = "Show summary stats") private static boolean showStats;

    @Argument(description = "Show tree after folding") private static boolean showTree;

    public static void main(String[] args) {
        Args.parseOrExit(Compiler.class, args);

        try {
            compileProgram(new FileInputStream(sourceName), new FileOutputStream(objectName), constantFolding, hoisting);
        } catch (FileNotFoundException e) {
            System.err.println("Could not open file: " + sourceName);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IOException while compiling: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (SyntaxError e) {
            System.err.println("SyntaxError while compiling: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void compileProgram(
            InputStream inputStream, final FileOutputStream outputStream, boolean constantFolding, boolean hoisting)
    throws IOException, SyntaxError {
        Parser parser = new Parser(inputStream);
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        Desugarer desugarer = new Desugarer();
        TypeChecker typeChecker = new TypeChecker();
        CodeGen codeGen = new CodeGen();
        ObjectWriter ObjectWriter = new ObjectWriter(new DataOutputStream(outputStream));

        Statement program = parser.parseProgram();

        // need to show stats *before* any desugaring, else counts will not be correct
        if (showStats) {
            SummaryVisitor summaryVisitor = new SummaryVisitor();
            if (summaryVisitor.generateSummary(program) instanceof SummaryVisitor.Summary(
                    int whileStatements, int ifStatements, int binaryOps
            )) {
                System.out.println("Summary: ");
                System.out.println("While Statements: " + whileStatements);
                System.out.println("If Statements: " + ifStatements);
                System.out.println("Binary Ops: " + binaryOps);
            }
        }

        // explicitly-typed AST
        semanticAnalyzer.analyzeAndType(program);

        if (!semanticAnalyzer.getErrors().isEmpty()) {
            System.err.println("Semantic analysis found errors, not proceeding with type checking");
            semanticAnalyzer.getErrors().forEach(System.err::println);
            return;
        }

        program = desugarer.desugar(program);

        typeChecker.typecheck(program);

        if (!typeChecker.getErrors().isEmpty()) {
            System.err.println("Type checker found errors, not proceeding with compilation");
            typeChecker.getErrors().forEach(System.err::println);
            return;
        }

        if (constantFolding) {
            program = Optimizer.foldConstants(program);

            if (showTree) {
                System.out.println(ASTPrinter.prettyPrint(program));
            }
        }

        if (hoisting) {
            program = Optimizer.hoist(program);
        }

        program = Optimizer.eliminateDeadCode(program);

        List<Instruction> ir = codeGen.generateInstructions(program);

        ir = Optimizer.threadJumps(ir);
        ir = Optimizer.combineInstructions(ir);

        List<Instruction.TAMInstruction> objectCode = Optimizer.backpatch(ir);

        ObjectWriter.write(objectCode);
    }

}
