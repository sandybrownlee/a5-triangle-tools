package triangle.codeGenerator.entities;

import triangle.abstractMachine.Machine;
import triangle.abstractMachine.OpCode;
import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Frame;

public class SquarePrimitiveRoutine extends RuntimeEntity implements RoutineEntity {

	public SquarePrimitiveRoutine() {
		super(Machine.closureSize);
	}

	public void encodeCall(Emitter emitter, Frame frame) {
		emitter.emit(OpCode.CALL, Register.PB, Primitive.SQUARE);
	}

	public void encodeFetch(Emitter emitter, Frame frame) {
		emitter.emit(OpCode.LOADA, 0, Register.SB, 0);
		emitter.emit(OpCode.LOADA, Register.PB, Primitive.SQUARE);
	}

}
