package triangle.codeGenerator.entities;

import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Frame;

@Deprecated public interface FetchableEntity {

    void encodeFetch(Emitter emitter, Frame frame, int size, Vname vname);

}
