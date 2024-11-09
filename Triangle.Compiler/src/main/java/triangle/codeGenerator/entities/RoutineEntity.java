package triangle.codeGenerator.entities;

import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.FrameOld;

@Deprecated public interface RoutineEntity {

    void encodeCall(Emitter emitter, FrameOld frame);

    void encodeFetch(Emitter emitter, FrameOld frame);

}
