package triangle.ast;

import java.util.List;

sealed public interface Type permits Type.ArrayType, Type.RecordType, Type.BasicType, Type.VoidType {

    record BasicType(String name) implements Type { }

    record ArrayType(int size, Type elementType) implements Type { }

    record RecordType(List<RecordFieldType> fieldTypes) implements Type {

        public record RecordFieldType(String fieldName, Type fieldType) { }

    }

    record VoidType() implements Type { }

    interface Visitor<ST,T> {
        T visit(ST state, Type type);
    }
}
