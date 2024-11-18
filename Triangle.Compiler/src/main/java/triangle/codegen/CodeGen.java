package triangle.codegen;

import triangle.repr.Instruction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public final class CodeGen {

    private final DataOutputStream outputStream;

    public CodeGen(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(final List<Instruction.TAMInstruction> instructions) throws IOException {
        for (Instruction.TAMInstruction i : instructions) {
            outputStream.writeInt(i.op());
            outputStream.writeInt(i.r());
            outputStream.writeInt(i.n());
            outputStream.writeInt(i.d());
        }
    }

}
