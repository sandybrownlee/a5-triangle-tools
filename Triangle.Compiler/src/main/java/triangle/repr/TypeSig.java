package triangle.repr;

import java.util.List;

// represents "syntactic" types, i.e.,
sealed public interface TypeSig permits TypeSig.ArrayTypeSig, TypeSig.BasicTypeSig, TypeSig.RecordTypeSig, TypeSig.Void {

    record BasicTypeSig(String name) implements TypeSig { }

    record ArrayTypeSig(int arraySize, TypeSig elementTypeSig) implements TypeSig { }

    record RecordTypeSig(List<FieldType> fieldTypes) implements TypeSig {

        public record FieldType(String fieldName, TypeSig fieldTypeSig) { }

    }

    record Void() implements TypeSig { }

}
