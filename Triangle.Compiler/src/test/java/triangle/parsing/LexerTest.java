package triangle.parsing;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static triangle.util.TestUtils.inputStreamOf;

public class LexerTest {

    @Test public void testTextToken() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("abcd1234"));

        TextToken t = (TextToken) lexer.nextToken();

        assertEquals(Token.Kind.IDENTIFIER, t.getKind());
        assertEquals("abcd1234", t.getText());
        assertEquals(Token.Kind.EOT, lexer.nextToken().getKind());
    }

    @Test public void testToken() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("while"));

        assertEquals(Token.Kind.WHILE, lexer.nextToken().getKind());
    }

    // lexer should EOT repeatedly after end of transmission
    @Test public void testEOT() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf(""));

        // only check repeated EOTs 10 times, this is probably good enough for anything we might want to do with Lexer
        for (int i = 0; i < 10; i++) {
            assertEquals(Token.Kind.EOT, lexer.nextToken().getKind());
        }
    }

    // lexer should have maximal munch
    @Test public void testMaximalMunching() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("whileeeee"));

        assertEquals(Token.Kind.IDENTIFIER, lexer.nextToken().getKind());
    }

    @Test public void testOperator() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("-%$!@#"));

        assertEquals(Token.Kind.OPERATOR, lexer.nextToken().getKind());
    }

    // lexer must split tokens at whitespace
    @Test public void testWhitespaceSplitting() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("while if abcd123"));

        assertEquals(Token.Kind.WHILE, lexer.nextToken().getKind());
        assertEquals(Token.Kind.IF, lexer.nextToken().getKind());
        assertEquals(Token.Kind.IDENTIFIER, lexer.nextToken().getKind());
    }

    // test line no.
    @Test public void testLineNo() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("line1\nline2\nline3"));

        assertEquals(1, lexer.nextToken().getLine());
        assertEquals(2, lexer.nextToken().getLine());
        assertEquals(3, lexer.nextToken().getLine());
    }

    // test col. no.
    @Test public void testColNo() throws IOException {
        Lexer lexer = new Lexer(inputStreamOf("abcd abcd abcd"));

        assertEquals(1, lexer.nextToken().getColumn());
        assertEquals(6, lexer.nextToken().getColumn());
        assertEquals(11, lexer.nextToken().getColumn());
    }
}
