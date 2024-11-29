package triangle.analysis;

import org.junit.jupiter.api.Test;
import triangle.parsing.Parser;
import triangle.parsing.SyntaxError;

import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static triangle.util.TestUtils.inputStreamOf;

public class SemanticAnalyzerTest {

    static {
        // TODO: we should use proper logging in the rest of the code and get rid of this
        System.setErr(new PrintStream(PrintStream.nullOutputStream()));
    }

    private static boolean analyzeExpectException(String code, Class<? extends SemanticException> excClass)
    throws SyntaxError, IOException {
        Parser parser = new Parser(inputStreamOf(code));
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();

        semanticAnalyzer.analyzeAndType(parser.parseProgram());

        for (SemanticException exc : semanticAnalyzer.getErrors()) {
            // exc instance <Expected Exception Class>
            if (excClass.equals(exc.getClass())) {
                return true;
            }
        }

        return false;
    }

    @Test public void testIntLitTooLarge() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException("99999", SemanticException.IntegerLiteralTooLarge.class));
    }

    @Test public void testDuplicateRecordField() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException("{ a ~ 10, a ~ 20}", SemanticException.DuplicateRecordField.class));
    }

    @Test public void testDuplicateRecordTypeField() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException(
                "let type R ~ record a : Integer, a : Char end in 1}",
                SemanticException.DuplicateRecordTypeField.class
        ));
    }

    @Test public void testAssignmentToConstant() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException("let const x ~ 10 in x := 1", SemanticException.AssignmentToConstant.class));
    }

    @Test public void testDuplicateDeclaration() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException(
                "let var x : Integer; var x : Integer in x",
                SemanticException.DuplicateDeclaration.class
        ));
    }

    @Test public void testDuplicateParameter() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException(
                "let proc p(x : Integer, x : Integer) ~ puteol() in 1",
                SemanticException.DuplicateParameter.class
        ));
    }

    @Test public void testUndeclaredUse() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException("x", SemanticException.UndeclaredUse.class));
    }

    @Test public void testInvalidArgument() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException(
                "let proc p(var x : Integer) ~ puteol(); var x : Integer in p(x)",
                SemanticException.InvalidArgument.class
        ));
    }

    @Test public void testNestingDepthExceeded() throws SyntaxError, IOException {
        assertTrue(analyzeExpectException(
                """
                let proc p1() ~
                    let proc p2() ~
                        let proc p3() ~
                            let proc p4() ~
                                let proc p5() ~
                                    let proc p6() ~
                                        let proc p7() ~
                                            let proc p8() ~ p1()
                                            in p8();
                                         in p7();
                                     in p6();
                                 in p5();
                             in p4();
                         in p3();
                     in p2();
                 in p1();
                """, SemanticException.NestingDepthExceeded.class
        ));
    }

}
