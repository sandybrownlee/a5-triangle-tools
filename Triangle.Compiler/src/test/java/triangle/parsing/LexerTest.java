package triangle.parsing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import triangle.Compiler;

public class LexerTest {

    // TODO: this test is harmful, we dont want to test for IOExceptions, test instead for whether or not lexer lexes
    //  identifiers etc correctly
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
            "/while-longloop.tri",
    })
    //@formatter:on
    @ParameterizedTest public void testLex(String filename) {
        Lexer lexer = new Lexer(Compiler.class.getResourceAsStream(filename));

        // lexer shouldn't throw exceptions
        Assertions.assertDoesNotThrow(() -> {
            while (lexer.nextToken().getKind() != Token.Kind.EOT) { }
        });
    }

}
