package triangle.codegen;

import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;

public sealed interface Instruction
        permits Instruction.CALL_LABEL, Instruction.JUMPIF_LABEL, Instruction.JUMP_LABEL, Instruction.LABEL,
                Instruction.LOADA_LABEL, Instruction.TAMInstruction {

    // since our IR is a strict superset of TAM instructions, model that fact with a simple nested sealed interface
    // any consumer of Instruction that is only interested in final TAM instructions can simply depend on this interface
    sealed interface TAMInstruction extends Instruction
            permits CALL, CALLI, HALT, JUMP, JUMPIF, LOAD, LOADA, LOADI, LOADL, POP, PUSH, RETURN, STORE, STOREI {

        // return the CALL instruction appropriate for calling the given Primitive
        static TAMInstruction callPrim(Primitive primitive) {
            // use anything as static link for primitive calls, it doesn't matter
            return new Instruction.CALL(Register.SB, new Address(Register.PB, primitive.ordinal()));
        }

    }

    record PUSH(int words) implements TAMInstruction { }

    record STORE(int words, Address address) implements TAMInstruction { }

    record STOREI(int size) implements TAMInstruction { }

    record LOAD(int words, Address address) implements TAMInstruction { }

    record LOADA(Address address) implements TAMInstruction { }

    record LOADL(int value) implements TAMInstruction { }

    record LOADI(int size) implements TAMInstruction { }

    record CALL(Register staticLink, Address address) implements TAMInstruction { }

    record RETURN(int resultSize, int argsSize) implements TAMInstruction { }

    record POP(int resultWords, int popCount) implements TAMInstruction { }

    record JUMP(Address address) implements TAMInstruction { }

    record JUMPIF(int value, Address address) implements TAMInstruction { }

    record CALLI() implements TAMInstruction { }

    record HALT() implements TAMInstruction { }

    // instructions specific to our IR; just includes labels and the ability to call/load/jump-to them

    record LABEL(int labelNo) implements Instruction { }

    record LOADA_LABEL(LABEL label) implements Instruction { }

    record JUMP_LABEL(LABEL label) implements Instruction { }

    record JUMPIF_LABEL(int value, LABEL label) implements Instruction { }

    record CALL_LABEL(Register staticLink, LABEL label) implements Instruction { }

    // d[r], in TAM terminology

    record Address(Register r, int d) { }

}
