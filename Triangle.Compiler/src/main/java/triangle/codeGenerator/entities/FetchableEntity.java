package triangle.codeGenerator.entities;

import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.FrameOld;

@Deprecated public interface FetchableEntity {

    void encodeFetch(Emitter emitter, FrameOld frame, int size, Vname vname);

}
