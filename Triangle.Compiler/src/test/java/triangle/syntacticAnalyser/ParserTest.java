package triangle.syntacticAnalyser;

import static org.junit.Assert.*;
import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.StringReader;
import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;

public class ParserTest {

  @Test
  public void testDecrementParsing() throws Exception {
    String prog =
      "let var a: Integer in\n" +
      "begin\n" +
      "  getint(var a);\n" +
      "  a--;\n" +
      "  putint(a);\n" +
      "  puteol();\n" +
      "  a--;\n" +
      "  putint(a);\n" +
      "  puteol();\n" +
      "end";

    // 1) write to a temp .tri file
    Path tmp = Files.createTempFile("decr", ".tri");
    Files.writeString(tmp, prog);

    // 2) point  Scanner at that path
    Scanner sc = new Scanner(SourceFile.ofPath(tmp.toString()));
    ErrorReporter rep = new ErrorReporter(false);
    Parser p = new Parser(sc, rep);

    p.parseProgram();
    assertEquals(0, rep.getNumErrors());
  }
  @Test
  public void testWhileCurlyParsing() throws Exception {
    String prog =
      "! print out aaaaa\n" +
      "\n" +
      "let\n" +
      "  var a : Integer\n" +
      "in\n" +
      "{\n" +
      "  a := 0;\n" +
      "  while a < 5 do\n" +
      "  {\n" +
      "    put('a');\n" +
      "    a := a + 1;\n" +
      "  }\n" +
      "}";
    // 1) write to a temp .tri file
    Path tmp = Files.createTempFile("whilecurly", ".tri");
    Files.writeString(tmp, prog);

    // 2) point your Scanner at that path
    Scanner sc = new Scanner(SourceFile.ofPath(tmp.toString()));
    ErrorReporter rep = new ErrorReporter(false);
    Parser p = new Parser(sc, rep);

    p.parseProgram();
    assertEquals("Curly‐brace while must parse cleanly", 0, rep.getNumErrors());
  }

}
