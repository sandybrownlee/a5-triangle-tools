package triangle.syntacticAnalyser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import triangle.syntacticAnalyzer.Scanner;

class TestScanner1 {

    @Test
    void testIsDigit_ValidDigits() {
        assertTrue(Scanner.isDigit('0'));
        assertTrue(Scanner.isDigit('5'));
        assertTrue(Scanner.isDigit('9'));
    }

    @Test
    void testIsDigit_InvalidDigits() {
        assertFalse(Scanner.isDigit('a'));
        assertFalse(Scanner.isDigit('Z'));
        assertFalse(Scanner.isDigit('!'));
        assertFalse(Scanner.isDigit(' '));
    }

    @Test
    void testIsLetter_ValidLetters() {
        assertTrue(Scanner.isLetter('a'));
        assertTrue(Scanner.isLetter('z'));
        assertTrue(Scanner.isLetter('A'));
        assertTrue(Scanner.isLetter('Z'));
    }

    @Test
    void testIsLetter_InvalidLetters() {
        assertFalse(Scanner.isLetter('0'));
        assertFalse(Scanner.isLetter('9'));
        assertFalse(Scanner.isLetter('#'));
        assertFalse(Scanner.isLetter(' '));
    }

    @Test
    void testIsDigit_BoundaryCases() {
        assertFalse(Scanner.isDigit('/')); // Before '0'
        assertFalse(Scanner.isDigit(':')); // After '9'
    }

    @Test
    void testIsLetter_BoundaryCases() {
        assertFalse(Scanner.isLetter('@')); // Before 'A'
        assertFalse(Scanner.isLetter('[')); // After 'Z'
        assertFalse(Scanner.isLetter('`')); // Before 'a'
        assertFalse(Scanner.isLetter('{')); // After 'z'
    }
}
