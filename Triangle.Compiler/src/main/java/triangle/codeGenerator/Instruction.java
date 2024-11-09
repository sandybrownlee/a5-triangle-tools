package triangle.codeGenerator;

import triangle.abstractMachine.Register;

public sealed interface Instruction
        permits Instruction.CALL, Instruction.HALT, Instruction.JUMP, Instruction.JUMPIF, Instruction.LABEL, Instruction.LOAD,
                Instruction.LOADA, Instruction.LOADA_LABEL, Instruction.LOADL, Instruction.POP, Instruction.PUSH,
                Instruction.RETURN, Instruction.STORE {

    record PUSH(int words) implements Instruction { }

    record STORE(int words, Address address) implements Instruction { }

    record LOAD(int words, Address address) implements Instruction {

        @Override public String toString() {
            return "\tLOAD (" + words + ") " + address;
        }

    }

    record LOADA(Address address) implements Instruction { }

    record LOADA_LABEL(LABEL address) implements Instruction { }

    record LOADL(int value) implements Instruction {

        @Override public String toString() {
            return "\tLOADL " + value;
        }

    }

    record CALL(Register staticLink, LABEL address) implements Instruction {

        @Override public String toString() {
            return "\tCALL (" + staticLink + ") " + address.labelNo;
        }

    }

    record RETURN(int resultSize, int argsSize) implements Instruction {

        @Override public String toString() {
            return "\tRETURN (" + resultSize + ") " + argsSize;
        }

    }

    record POP(int resultWords, int popCount) implements Instruction {

        @Override public String toString() {
            return "\tPOP (" + resultWords + ") " + popCount;
        }

    }

    record JUMP(LABEL label) implements Instruction {

        @Override public String toString() {
            return "\tJUMP " + label.labelNo;
        }

    }

    record JUMPIF(int value, LABEL label) implements Instruction { }

    record HALT() implements Instruction {

        @Override public String toString() {
            return "\tHALT";
        }

    }

    record LABEL(int labelNo) implements Instruction {

        @Override public String toString() {
            return labelNo + ":";
        }

    }

}
