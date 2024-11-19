package triangle.codegen;

import triangle.abstractMachine.Register;
import triangle.repr.Instruction;
import triangle.repr.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Optimizer {

    public static Statement foldConstants(Statement program) {
        ConstantFolder constantFolder = new ConstantFolder();
        return constantFolder.fold(program);
    }

    public static Statement hoist(Statement program) {
        Hoister hoister = new Hoister();
        return hoister.hoist(program);
    }

    // TODO: this is O(instructions.size()^2), use union-find here
    public static List<Instruction> threadJumps(List<Instruction> instructions) {
        Map<Instruction.LABEL, Instruction.LABEL> threadable = new HashMap<>();

        for (int i = 0; i < instructions.size() - 1; i++) {
            Instruction cur = instructions.get(i);
            Instruction next = instructions.get(i + 1);

            if (cur instanceof Instruction.LABEL curLabel && next instanceof Instruction.JUMP_LABEL(Instruction.LABEL jumpedLabel)) {
                threadable.put(curLabel, jumpedLabel);
            }
        }

        boolean flag = true;
        while (flag) {
            flag = false;
            for (Map.Entry<Instruction.LABEL, Instruction.LABEL> entry : threadable.entrySet()) {
                if (threadable.containsKey(entry.getValue())) {
                    threadable.replace(entry.getKey(), threadable.get(entry.getValue()));
                    flag = true;
                }
            }
        }

        List<Instruction> threaded = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);

            if (instruction instanceof Instruction.LABEL label && threadable.containsKey(label)) {
                // if there is a threadable jump here, then skip the next instruction
                i++;
                continue;
            }

            threaded.add(switch (instruction) {
                case Instruction.CALL_LABEL(Register staticLink, Instruction.LABEL label) when threadable.containsKey(label) ->
                        new Instruction.CALL_LABEL(staticLink, threadable.get(label));
                case Instruction.JUMPIF_LABEL(int value, Instruction.LABEL label) when threadable.containsKey(label) ->
                        new Instruction.JUMPIF_LABEL(value, threadable.get(label));
                case Instruction.JUMP_LABEL(Instruction.LABEL label) when threadable.containsKey(label) ->
                        new Instruction.JUMP_LABEL(threadable.get(label));
                case Instruction.LOADA_LABEL(Instruction.LABEL label) when threadable.containsKey(label) ->
                        new Instruction.LOADA_LABEL(threadable.get(label));
                default -> instruction;
            });
        }

        return threaded;
    }

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
}
