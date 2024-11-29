package triangle.codegen;

import triangle.abstractMachine.Machine;
import triangle.abstractMachine.Primitive;
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

    public static Statement eliminateDeadCode(Statement program) {
        DeadCodeEliminator deadCodeEliminator = new DeadCodeEliminator();
        return deadCodeEliminator.eliminateDeadCode(program);
    }

    // thread unconditional jumps, to reduce some code bloat.
    public static List<Instruction> threadJumps(List<Instruction> instructions) {
        Map<Instruction.LABEL, Instruction.LABEL> threadable = new HashMap<>();

        for (int i = 0; i < instructions.size() - 1; i++) {
            Instruction cur = instructions.get(i);
            Instruction next = instructions.get(i + 1);

            if (cur instanceof Instruction.LABEL curLabel &&
                next instanceof Instruction.JUMP_LABEL(Instruction.LABEL jumpedLabel)) {
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

    // this is quite ad-hoc right now
    @SuppressWarnings("DanglingJavadoc") public static List<Instruction> combineInstructions(List<Instruction> instructions) {
        List<Instruction> combined = new ArrayList<>();

        for (int i = 0; i < instructions.size() - 2; i++) {
            Instruction cur = instructions.get(i);
            Instruction next = instructions.get(i + 1);
            Instruction next2 = instructions.get(i + 2);

            //// [LOADL x, LOADL y, CALL_PRIM add, ...] when inRange(x + y) -> [LOADL x+y, ...]
            if (cur instanceof Instruction.LOADL(int x) &&
                next instanceof Instruction.LOADL(int y) &&
                next2 instanceof Instruction.CALL_PRIM(Primitive p) &&
                p == Primitive.ADD &&
                inRange(x + y)
            ) {
                combined.add(new Instruction.LOADL(x + y));
                continue;
            }

            //// [LOADL x, LOADL y, CALL_PRIM mult, ...] when inRange(x * y) -> [LOADL x*y, ...]
            if (cur instanceof Instruction.LOADL(int x) &&
                next instanceof Instruction.LOADL(int y) &&
                next2 instanceof Instruction.CALL_PRIM(Primitive p) &&
                p == Primitive.MULT &&
                inRange(x * y)
            ) {
                combined.add(new Instruction.LOADL(x * y));
                continue;
            }

            combined.add(cur);
        }

        combined.add(instructions.get(instructions.size() - 2));
        //noinspection SequencedCollectionMethodCanBeUsed
        combined.add(instructions.get(instructions.size() - 1));

        return combined;
    }

    //  resolve all labels, primitive calls, redundant instruction etc.
    public static List<Instruction.TAMInstruction> resolveLabels(final List<Instruction> instructions) {
        Function<Instruction.LABEL, Instruction.Address> toCodeAddress = generateLabelToAddressMapper(instructions);

        List<Instruction.TAMInstruction> resolved = new ArrayList<>();
        for (Instruction instruction : instructions) {
            switch (instruction) {
                case Instruction.LABEL _ -> { }
                case Instruction.POP(int resultWords, int popCount) when resultWords == 0 && popCount == 0 -> { }
                case Instruction.CALL_LABEL(Register staticLink, Instruction.LABEL label) -> resolved.add(
                        new Instruction.CALL(staticLink, toCodeAddress.apply(label)));
                case Instruction.JUMPIF_LABEL(int value, Instruction.LABEL label) -> resolved.add(
                        new Instruction.JUMPIF(value, toCodeAddress.apply(label)));
                case Instruction.JUMP_LABEL(Instruction.LABEL label) -> resolved.add(
                        new Instruction.JUMP(toCodeAddress.apply(label)));
                case Instruction.LOADA_LABEL(Instruction.LABEL label) -> resolved.add(
                        new Instruction.LOADA(toCodeAddress.apply(label)));
                case Instruction.CALL_PRIM(Primitive p) -> resolved.add(
                        new Instruction.CALL(Register.SB, new Instruction.Address(Register.PB, p.ordinal())));
                case Instruction.TAMInstruction tamInstruction -> resolved.add(tamInstruction);
            }
        }
        return resolved;
    }

    // need to ensure combined primitive calls don't overflow
    private static boolean inRange(int x) {
        return x >= (Machine.maxintRep * -1) && x <= Machine.maxintRep;
    }

    private static Function<Instruction.LABEL, Instruction.Address> generateLabelToAddressMapper(
            final List<Instruction> instructions
    ) {
        Map<Instruction.LABEL, Integer> labelLocations = new HashMap<>();

        int offset = 0;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);

            // remove redundant instructions
            if (instruction instanceof Instruction.POP(int resultWords, int popCount) && resultWords == 0 && popCount == 0) {
                offset += 1;
                continue;
            }

            if (instruction instanceof Instruction.LABEL label) {
                labelLocations.put(label, i - offset);
                offset += 1;
            }
        }

        // so I don't have to type 'new Address ...' repeatedly
        return label -> new Instruction.Address(
                Register.CB,
                labelLocations.get(label)
        );
    }

}
