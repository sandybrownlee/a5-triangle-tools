package triangle.parsing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import triangle.Compiler;
import triangle.repr.Expression.BinaryOp;
import triangle.repr.Expression.Identifier.ArraySubscript;
import triangle.repr.Expression.Identifier.BasicIdentifier;
import triangle.repr.Expression.Identifier.RecordAccess;
import triangle.repr.Expression.LitInt;
import triangle.repr.Statement;
import triangle.repr.Statement.ExpressionStatement;

import java.io.IOException;
import static triangle.util.TestUtils.inputStreamOf;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    //@formatter:off
    @ValueSource(strings = {
            "/adddeep.tri",
            "/arrays.tri",
            "/assignments.tri",
            "/bank.tri",
            "/bardemo.tri",
            "/complexrecord.tri",
            "/control.tri",
            "/deepnest.tri",
            "/directories.tri",
            "/errors.tri",
            "/every.tri",
            "/factorials.tri",
            "/foldable.tri",
            "/functions.tri",
            "/hi.tri",
            "/hi-newcomment.tri",
            "/hi-newcomment2.tri",
            "/hoistable.tri",
            "/hullo.tri",
            "/ifdemo.tri",
            "/increment.tri",
            "/loopwhile.tri",
            "/names.tri",
            "/nesting.tri",
            "/procedural.tri",
            "/procedures.tri",
            "/procparam.tri",
            "/records.tri",
            "/repeatuntil.tri",
            "/repl.tri",
            "/simpleadding.tri",
            "/triangle.tri",
            "/unaryops.tri",
            "/varinvar.tri",
            "/while.tri",
            "/while-longloop.tri",})
    //@formatter:on
    @ParameterizedTest public void testParse(String filename) {
        Assertions.assertDoesNotThrow(() -> new Parser(Compiler.class.getResourceAsStream(filename)).parseProgram());
    }

    // test binops are parsed right-associatively
    @Test public void testBinopRightAssociative() throws IOException, SyntaxError {
        // should parse as: add
        //                  / \
        //                 4  mul
        //                    / \
        //                   3  div
        //                      / \
        //                     5  mod
        //                        / \
        //                       8   2
        Statement program = new Parser(inputStreamOf("4 + 3 * 5 / 8 // 2")).parseProgram();

        // unpack the parse tree manually
        try {
            // EXPR = 4 + RIGHT_0
            BinaryOp expr = (BinaryOp) ((ExpressionStatement) program).expression();
            LitInt four = (LitInt) expr.leftOperand();
            String add = expr.operator();

            // RIGHT_0 = 3 * RIGHT_1
            BinaryOp right_0 = (BinaryOp) expr.rightOperand();
            LitInt three = (LitInt) right_0.leftOperand();
            String mul = right_0.operator();

            // RIGHT_1 = 5 / RIGHT_2
            BinaryOp right_1 = (BinaryOp) right_0.rightOperand();
            LitInt five = (LitInt) right_1.leftOperand();
            String div = right_1.operator();

            // RIGHT_2 = 8 // 2
            BinaryOp right_2 = (BinaryOp) right_1.rightOperand();
            LitInt eight = (LitInt) right_2.leftOperand();
            String mod = right_2.operator();
            LitInt two = (LitInt) right_2.rightOperand();

            // asserts everything is what we expected it to be

            assertEquals("+", add);
            assertEquals("*", mul);
            assertEquals("/", div);
            assertEquals("//", mod);

            assertEquals(4, four.value());
            assertEquals(3, three.value());
            assertEquals(5, five.value());
            assertEquals(8, eight.value());
            assertEquals(2, two.value());
        } catch (ClassCastException e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

    // test nested record access is parsed right-associatively
    @Test public void testNestedRecordAccessRightAssociative() throws IOException, SyntaxError {
        // should parse as: RecordAcc
        //                   /    \
        //              outer      RecordAcc
        //                          /    \
        //                   middle       inner
        Statement program = new Parser(inputStreamOf("outer.middle.inner")).parseProgram();

        // unpack the parse tree manually
        try {
            // EXPR = outer.RIGHT_1
            RecordAccess expr = (RecordAccess) ((ExpressionStatement) program).expression();
            BasicIdentifier outer = (BasicIdentifier) expr.record();

            // RIGHT_1 = middle.inner
            RecordAccess right_1 = (RecordAccess) expr.field();
            BasicIdentifier middle = (BasicIdentifier) right_1.record();
            BasicIdentifier inner = (BasicIdentifier) right_1.field();

            // asserts everything is what we expected it to be

            assertEquals("outer", outer.name());
            assertEquals("middle", middle.name());
            assertEquals("inner", inner.name());
        } catch (ClassCastException e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

    // tests complex record expressions (records-in-arrays and arrays-in-records) are parsed correctly
    @Test public void testComplexRecordExpression() throws IOException, SyntaxError {
        // should parse as: RecordAcc
        //                  /       \
        //          ArraySub         RecordAcc
        //           /   \          /         \
        //        outer   0     middle      RecordAcc
        //                                  /        \
        //                           ArraySub       field
        //                            /   \
        //                        inner     4
        Statement program = new Parser(inputStreamOf("outer[0].middle.inner[4].field")).parseProgram();

        // unpack the parse tree manually
        try {
            // EXPR = LEFT_0.RIGHT_1
            RecordAccess expr = (RecordAccess) ((ExpressionStatement) program).expression();

            // LEFT_0 = outer[0]
            ArraySubscript left_0 = (ArraySubscript) expr.record();
            BasicIdentifier outer = (BasicIdentifier) left_0.array();
            LitInt zero = (LitInt) left_0.subscript();

            // RIGHT_1 = middle.RIGHT_2
            RecordAccess right_1 = (RecordAccess) expr.field();
            BasicIdentifier middle = (BasicIdentifier) right_1.record();

            // RIGHT_2 = LEFT_1.field
            RecordAccess right_2 = (RecordAccess) right_1.field();

            // LEFT_1 = inner[4]
            ArraySubscript left_1 = (ArraySubscript) right_2.record();
            BasicIdentifier inner = (BasicIdentifier) left_1.array();
            LitInt four = (LitInt) left_1.subscript();

            BasicIdentifier field = (BasicIdentifier) right_2.field();

            // asserts everything is what we expected it to be

            assertEquals("outer", outer.name());
            assertEquals("middle", middle.name());
            assertEquals("inner", inner.name());
            assertEquals("field", field.name());

            assertEquals(0, zero.value());
            assertEquals(4, four.value());
        } catch (ClassCastException e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

}
