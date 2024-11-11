package triangle.codegen;

import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;

public sealed interface Instruction
        permits Instruction.CALL, Instruction.CALLI, Instruction.CALL_LABEL, Instruction.CALL_PRIM, Instruction.HALT,
                Instruction.JUMP, Instruction.JUMPIF, Instruction.JUMPIF_LABEL, Instruction.JUMP_LABEL, Instruction.LABEL,
                Instruction.LOAD, Instruction.LOADA, Instruction.LOADA_LABEL, Instruction.LOADI, Instruction.LOADL,
                Instruction.POP, Instruction.PUSH, Instruction.RETURN, Instruction.STORE, Instruction.STOREI {

    record PUSH(int words) implements Instruction { }

    record STORE(int words, Address address) implements Instruction { }

    record STOREI(int size) implements Instruction { }

    record LOAD(int words, Address address) implements Instruction { }

    record LOADA(Address address) implements Instruction { }

    record LOADL(int value) implements Instruction { }

    record LOADI(int size) implements Instruction { }

    record CALL(Register staticLink, Address address) implements Instruction { }

    record RETURN(int resultSize, int argsSize) implements Instruction { }

    record POP(int resultWords, int popCount) implements Instruction { }

    record JUMP(Address address) implements Instruction { }

    record JUMPIF(int value, Address address) implements Instruction { }

    record CALLI() implements Instruction { }

    record HALT() implements Instruction { }

    // pseudo-instructions, to be converted to primitive calls later

    // TODO: CALL_PRIM can probably be removed
    record CALL_PRIM(Primitive primitive) implements Instruction { }

    // pseudo-instructions corresponding to symbolic addresses

    record LABEL(int labelNo) implements Instruction { }

    record LOADA_LABEL(LABEL label) implements Instruction { }

    record JUMP_LABEL(LABEL label) implements Instruction { }

    record JUMPIF_LABEL(int value, LABEL label) implements Instruction { }

    record CALL_LABEL(Register staticLink, LABEL label) implements Instruction { }

    record Address(Register r, int d) { }

}
