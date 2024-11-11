package triangle.codegen;

import triangle.abstractMachine.Register;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CodeGen {

    static final Map<String, IRGenerator.Callable> primitives = new HashMap<>();

    public static void write(final List<Instruction.TAMInstruction> instructions) throws IOException {
        try (DataOutputStream fw = new DataOutputStream(new FileOutputStream("obj.tam"))) {
            for (Instruction.TAMInstruction instruction : instructions) {
                // TODO: this is too ugly to stay + fill in
                switch (instruction) {
                    case Instruction.CALL call -> {
                        fw.writeInt(6);
                        fw.writeInt(call.address().r().ordinal());
                        fw.writeInt(call.staticLink().ordinal());
                        fw.writeInt(call.address().d());
                    }
                    case Instruction.CALLI calli -> {
                        fw.writeInt(7);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(0);
                    }
                    case Instruction.HALT halt -> {
                        fw.writeInt(15);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(0);
                    }
                    case Instruction.JUMP jump -> {
                        fw.writeInt(12);
                        fw.writeInt(jump.address().r().ordinal());
                        fw.writeInt(0);
                        fw.writeInt(jump.address().d());
                    }
                    case Instruction.JUMPIF jumpif -> {
                        fw.writeInt(14);
                        fw.writeInt(jumpif.address().r().ordinal());
                        fw.writeInt(jumpif.value());
                        fw.writeInt(jumpif.address().d());
                    }
                    case Instruction.LOAD load -> {
                        fw.writeInt(0);
                        fw.writeInt(load.address().r().ordinal());
                        fw.writeInt(load.words());
                        fw.writeInt(load.address().d());
                    }
                    case Instruction.LOADA loada -> {
                        fw.writeInt(1);
                        fw.writeInt(loada.address().r().ordinal());
                        fw.writeInt(0);
                        fw.writeInt(loada.address().d());
                    }
                    case Instruction.LOADI loadi -> {
                        fw.writeInt(2);
                        fw.writeInt(0);
                        fw.writeInt(loadi.size());
                        fw.writeInt(0);
                    }
                    case Instruction.LOADL loadl -> {
                        fw.writeInt(3);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(loadl.value());
                    }
                    case Instruction.POP pop -> {
                        fw.writeInt(11);
                        fw.writeInt(0);
                        fw.writeInt(pop.resultWords());
                        fw.writeInt(pop.popCount());
                    }
                    case Instruction.PUSH push -> {
                        fw.writeInt(10);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(push.words());
                    }
                    case Instruction.RETURN aReturn -> {
                        fw.writeInt(8);
                        fw.writeInt(0);
                        fw.writeInt(aReturn.resultSize());
                        fw.writeInt(aReturn.argsSize());
                    }
                    case Instruction.STORE store -> {
                        fw.writeInt(4);
                        fw.writeInt(store.address().r().ordinal());
                        fw.writeInt(store.words());
                        fw.writeInt(store.address().d());
                    }
                    case Instruction.STOREI storei -> {
                        fw.writeInt(5);
                        fw.writeInt(0);
                        fw.writeInt(storei.size());
                        fw.writeInt(0);
                    }
                }
            }
        }
    }

    // TODO: maybe do some optimizations before backpatching? eg. any label immediately followed by a jump can just edit all jumps
    //  to that label
    // backpatch the instruction list to resolve all labels, etc.
    public static List<Instruction.TAMInstruction> backpatch(final List<Instruction> instructions) {
        Map<Instruction.LABEL, Integer> labelLocations = new HashMap<>();

        int offset = 0;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof Instruction.LABEL label) {
                labelLocations.put(label, i - offset);
                offset += 1;
            }
        }

        // @formatter:off
        // so I don't have to type 'new Address ...' repeatedly
        Function<Instruction.LABEL, Instruction.Address> toCodeAddress = label -> new Instruction.Address(
                Register.CB,
                labelLocations.get(label)
        );
        // @formatter:on

        List<Instruction.TAMInstruction> patchedInstructions = new ArrayList<>();
        for (Instruction instruction : instructions) {
            switch (instruction) {
                case Instruction.LABEL _ -> { }
                case Instruction.CALL_LABEL(Register staticLink, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.CALL(staticLink, toCodeAddress.apply(label)));
                case Instruction.JUMPIF_LABEL(int value, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMPIF(value, toCodeAddress.apply(label)));
                case Instruction.JUMP_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMP(toCodeAddress.apply(label)));
                case Instruction.LOADA_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.LOADA(toCodeAddress.apply(label)));
                case Instruction.TAMInstruction tamInstruction -> patchedInstructions.add(tamInstruction);
            }
        }

        return patchedInstructions;
    }

    private CodeGen() {
        throw new IllegalStateException("Utility class");
    }

}
