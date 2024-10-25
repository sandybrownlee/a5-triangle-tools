package triangle.ast;

import java.util.List;

sealed public interface Type permits Expression.Identifier, Type.ArrayType, Type.RecordType {

    record ArrayType(int size, Type elementType) implements Type { }

    record RecordType(List<RecordFieldType> fieldTypes) implements Type {

        record RecordFieldType(Expression.Identifier name, Type type) { }

    }

}
