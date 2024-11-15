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
import triangle.analysis.SemanticAnalyzer;
import triangle.analysis.SemanticException;
import triangle.codegen.CodeGen;
import triangle.codegen.IRGenerator;
import triangle.codegen.Instruction;
import triangle.parsing.Lexer;
import triangle.parsing.Parser;
import triangle.parsing.SyntaxError;
import triangle.repr.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// TODO: replace ErrorReporter with robust logging
// TODO: showStats cmdline option
public class Compiler {

    /** The filename for the object program, normally obj.tam. */
    @Argument(description = "The name of the file to store the object code into") static String objectName = "obj.tam";

    @Argument(description = "The name of the source file to compile", required = true) private static String sourceName;

    @Argument(description = "Whether or not to show the tree") private static boolean showTree;

    public static void main(String[] args) {
        Args.parseOrExit(Compiler.class, args);

        try {
//            for (String fileName : new File("programs/").list()) {
//                System.out.println("programs/" + fileName);
//                compileProgram(new FileInputStream("programs/" + fileName));
//                System.in.read();
//            }
            compileProgram(new FileInputStream("programs/procparam.tri"));
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

    static void compileProgram(InputStream inputStream) throws IOException, SyntaxError {
        Lexer lexer = new Lexer(inputStream);
        Parser parser = new Parser(lexer);

        Statement ast = parser.parseProgram();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();

        List<SemanticException> errors = semanticAnalyzer.analyzeProgram(ast);

        if (!errors.isEmpty()) {
            errors.forEach(System.err::println);
            return;
        }

        IRGenerator IRGenerator = new IRGenerator();
        List<Instruction> ir = IRGenerator.generateIR(ast);

        CodeGen.write("obj.tam", CodeGen.backpatch(ir));
    }

}
