package triangle.ast;

import java.util.List;

// represents "syntactic" types, i.e.,
sealed public interface Type permits Type.ArrayType, Type.BasicType, Type.RecordType, Type.Void {

    record BasicType(String name) implements Type { }

    record ArrayType(int arraySize, Type elementType) implements Type { }

    record RecordType(List<FieldType> fieldTypes) implements Type {

        public record FieldType(String fieldName, Type fieldType) { }

    }

    record Void() implements Type { }

}
