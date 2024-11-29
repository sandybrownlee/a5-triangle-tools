package triangle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileOutputStream;

public class CompilerTest {

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
    @ParameterizedTest public void testCompile(String filename) {
        Compiler.folding = true;
        Compiler.hoisting = true;
        Assertions.assertDoesNotThrow(() -> Compiler.compileProgram(
                Compiler.class.getResourceAsStream(filename),
                new FileOutputStream("obj.tam")
        ));
    }

}
