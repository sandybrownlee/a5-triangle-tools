package triangle.codegen;

import triangle.abstractMachine.Register;
import triangle.repr.Statement;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// TODO: split optimization and writing out
// TODO: parameterize CodeGen with DataOutputStream
public final class CodeGen {

    public static void write(final String objName, final List<Instruction.TAMInstruction> instructions) throws IOException {
        try (DataOutputStream fw = new DataOutputStream(new FileOutputStream(objName))) {
            for (Instruction.TAMInstruction i : instructions) {
                fw.writeInt(i.op());
                fw.writeInt(i.r());
                fw.writeInt(i.n());
                fw.writeInt(i.d());
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

    public static List<Instruction> generate(Statement statement) {
        ConstantFolder constantFolder = new ConstantFolder();
        IRGenerator irGenerator = new IRGenerator();
        Hoister hoister = new Hoister();

        return irGenerator.generateIR(hoister.hoist(constantFolder.fold(statement)));
    }

}
