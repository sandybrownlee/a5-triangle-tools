package triangle.codeGenerator;

import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;

public sealed interface Instruction
        permits Instruction.CALL, Instruction.CALLI, Instruction.CALL_LABEL, Instruction.CALL_PRIM, Instruction.HALT,
                Instruction.JUMP, Instruction.JUMPIF, Instruction.JUMPIF_LABEL, Instruction.JUMP_LABEL, Instruction.LABEL,
                Instruction.LOAD, Instruction.LOADA, Instruction.LOADA_LABEL, Instruction.LOADI, Instruction.LOADL,
                Instruction.POP, Instruction.PUSH, Instruction.RETURN, Instruction.STORE, Instruction.STOREI {

    record PUSH(int words) implements Instruction {

        @Override public String toString() {
            return "PUSH " + words;
        }

    }

    record STORE(int words, Address address) implements Instruction { }

    record STOREI(int size) implements Instruction {

        @Override public String toString() {
            return "STOREI " + size;
        }

    }

    record LOAD(int words, Address address) implements Instruction {

        @Override public String toString() {
            return "LOAD (" + words + ") " + address;
        }

    }

    record LOADA(Address address) implements Instruction {

        @Override public String toString() {
            return "LOADA " + address.d() + " [" + address.r() + "]";
        }

    }

    record LOADL(int value) implements Instruction {

        @Override public String toString() {
            return "LOADL " + value;
        }

    }

    record LOADI(int size) implements Instruction {

        @Override public String toString() {
            return "LOADI " + size;
        }

    }

    record CALL(Register staticLink, Address address) implements Instruction {

        @Override public String toString() {
            return "CALL (" + staticLink + ")" + address;
        }

    }

    record RETURN(int resultSize, int argsSize) implements Instruction {

        @Override public String toString() {
            return "RETURN (" + resultSize + ") " + argsSize;
        }

    }

    record POP(int resultWords, int popCount) implements Instruction {

        @Override public String toString() {
            return "POP (" + resultWords + ") " + popCount;
        }

    }

    record JUMP(Address address) implements Instruction {

        @Override public String toString() {
            return "JUMP " + address;
        }

    }

    record JUMPIF(int value, Address address) implements Instruction { }

    record CALLI() implements Instruction {

        @Override public String toString() {
            return "CALLI";
        }

    }

    record HALT() implements Instruction {

        @Override public String toString() {
            return "HALT";
        }

    }

    // pseudo-instruction, to be converted to primitive calls later
    // TODO: CALL_PRIM can probably be removed
    record CALL_PRIM(Primitive primitive) implements Instruction {

        @Override public String toString() {
            return "CALL_PRIM " + primitive;
        }

    }

    // psuedo-instructions corresponding to symbolic addresses

    record LABEL(int labelNo) implements Instruction {

        @Override public String toString() {
            return labelNo + ":";
        }

    }

    record LOADA_LABEL(LABEL label) implements Instruction {

        @Override public String toString() {
            return "LOADA @" + label.labelNo;
        }

    }

    record JUMP_LABEL(LABEL label) implements Instruction {

        @Override public String toString() {
            return "JUMP_LABEL @" + label.labelNo;
        }

    }

    record JUMPIF_LABEL(int value, LABEL label) implements Instruction { }

    record CALL_LABEL(Register staticLink, LABEL label) implements Instruction {

        @Override public String toString() {
            return "CALL_LABEL (" + staticLink + ") @" + label.labelNo;
        }

    }

}
