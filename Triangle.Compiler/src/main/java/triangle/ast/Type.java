package triangle.ast;

import java.util.List;

sealed public interface Type permits Type.ArrayType, Type.BasicType, Type.PrimType, Type.RecordType {

    // static instances of primitive types for convenience
    Type BOOL_TYPE = new PrimType.BoolType();
    Type INT_TYPE  = new PrimType.IntType();
    Type CHAR_TYPE = new PrimType.CharType();
    Type VOID_TYPE = new PrimType.VoidType();

    sealed interface PrimType extends Type permits PrimType.FuncType, PrimType.BoolType, PrimType.IntType, PrimType.CharType,
                                                   PrimType.VoidType {

        record FuncType(List<Type> argTypes, Type returnType) implements PrimType { }

        record BoolType() implements PrimType { }

        record IntType() implements PrimType { }

        record CharType() implements PrimType { }

        record VoidType() implements PrimType { }

    }

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Type type) throws E;

    }

    record BasicType(String name) implements Type { }

    record ArrayType(int size, Type elementType) implements Type { }

    record RecordType(List<RecordFieldType> fieldTypes) implements Type {

        public record RecordFieldType(String fieldName, Type fieldType) { }

    }

}
