package triangle.repr;

import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;

public sealed interface Instruction {

    // since our IR is a strict superset of TAM instructions, model that fact with a simple nested sealed interface
    // any consumer of Instruction that is only interested in final TAM instructions can simply depend on this interface
    sealed interface TAMInstruction extends Instruction {
        // unused fields are set to 0
        // in the interest of performance, some of the overrides of this method cast an int value to a short which will
        // silently fail if the int value exceeds short's width; however, the rest of the compiler should ensure no such value
        // exists since it is disallowed by the TAM specification

        // 4 bits
        short op();

        // 4 bits
        default short r() {
            return 0;
        }

        // 8 bits
        default short n() {
            return 0;
        }

        // 16 bits
        default short d() {
            return 0;
        }

    }

    record LOAD(int words, Address address) implements TAMInstruction {

        @Override public short op() {
            return 0;
        }

        @Override public short n() {
            return (short) words;
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record LOADA(Address address) implements TAMInstruction {

        @Override public short op() {
            return 1;
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record LOADI(int size) implements TAMInstruction {

        @Override public short op() {
            return 2;
        }

        @Override public short n() {
            return (short) size;
        }

    }

    record LOADL(int value) implements TAMInstruction {

        @Override public short op() {
            return 3;
        }

        @Override public short d() {
            return (short) value;
        }

    }

    record STORE(int words, Address address) implements TAMInstruction {

        @Override public short op() {
            return 4;
        }

        @Override public short n() {
            return (short) words;
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record STOREI(int size) implements TAMInstruction {

        @Override public short op() {
            return 5;
        }

        @Override public short n() {
            return (short) size;
        }

    }

    record CALL(Register staticLink, Address address) implements TAMInstruction {

        @Override public short op() {
            return 6;
        }

        @Override public short n() {
            return (short) staticLink.ordinal();
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record CALLI() implements TAMInstruction {

        @Override public short op() {
            return 7;
        }

    }

    record RETURN(int resultSize, int argsSize) implements TAMInstruction {

        @Override public short op() {
            return 8;
        }

        @Override public short n() {
            return (short) resultSize;
        }

        @Override public short d() {
            return (short) argsSize;
        }

    }

    record UNUSED() implements TAMInstruction {

        @Override public short op() {
            return 9;
        }

    }

    record PUSH(int words) implements TAMInstruction {

        @Override public short op() {
            return 10;
        }

        @Override public short d() {
            return (short) words;
        }

    }

    record POP(int resultWords, int popCount) implements TAMInstruction {

        @Override public short op() {
            return 11;
        }

        @Override public short n() {
            return (short) resultWords;
        }

        @Override public short d() {
            return (short) popCount;
        }

    }

    record JUMP(Address address) implements TAMInstruction {

        @Override public short op() {
            return 12;
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record JUMPI() implements TAMInstruction {

        @Override public short op() {
            return 13;
        }

    }

    record JUMPIF(int value, Address address) implements TAMInstruction {

        @Override public short op() {
            return 14;
        }

        @Override public short n() {
            return (short) value;
        }

        @Override public short d() {
            return (short) address.d;
        }

        @Override public short r() {
            return (short) address.r().ordinal();
        }

    }

    record HALT() implements TAMInstruction {

        @Override public short op() {
            return 15;
        }

    }

    // instructions specific to our IR; just includes labels and the ability to call/load/jump-to them

    record LABEL(int labelNo) implements Instruction { }

    record LOADA_LABEL(LABEL label) implements Instruction { }

    record JUMP_LABEL(LABEL label) implements Instruction { }

    record JUMPIF_LABEL(int value, LABEL label) implements Instruction { }

    record CALL_LABEL(Register staticLink, LABEL label) implements Instruction { }

    record CALL_PRIM(Primitive p) implements Instruction { }

    // d[r], in TAM terminology

    record Address(Register r, int d) { }

}
