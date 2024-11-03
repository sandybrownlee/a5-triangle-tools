package triangle.ast;

import java.util.List;

sealed public interface Type
        permits Type.ArrayType, Type.BasicType, Type.BoolType, Type.CharType, Type.IntType, Type.RecordType, Type.VoidType {

    record BasicType(String name) implements Type { }

    record ArrayType(int size, Type elementType) implements Type { }

    record RecordType(List<RecordFieldType> fieldTypes) implements Type {

        public record RecordFieldType(String fieldName, Type fieldType) { }

    }

    record BoolType() implements Type { }
    record IntType() implements Type { }
    record CharType() implements Type { }
    record VoidType() implements Type { }

    interface Visitor<ST,T> {
        T visit(ST state, Type type);
    }
}
