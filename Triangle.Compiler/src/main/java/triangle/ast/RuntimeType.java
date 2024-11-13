package triangle.ast;

import triangle.abstractMachine.Machine;

import java.util.List;

// interface that represents types that are "resolved", i.e.:
//      type P ~ record x : Integer end
//      type R ~ record x : P, y : P end
// is resolved into
//      type P ~ record x : Integer end
//      type R ~ record x : (record x : Integer), y : (record x : Integer) end
sealed public interface RuntimeType {

    // static instances of primitive types for convenience
    RuntimeType BOOL_TYPE = new PrimType.BoolType();
    RuntimeType INT_TYPE  = new PrimType.IntType();
    RuntimeType CHAR_TYPE = new PrimType.CharType();
    RuntimeType VOID_TYPE = new PrimType.VoidType();

    int size();

    // e.g: baseType(RefOf(RefOf(Integer)) = Integer
    default RuntimeType baseType() {
        return this;
    }

    sealed interface PrimType extends RuntimeType
            permits PrimType.FuncType, PrimType.BoolType, PrimType.IntType, PrimType.CharType,
                    PrimType.VoidType {

        record FuncType(List<RuntimeType> argTypes, RuntimeType returnType) implements PrimType {

            @Override public int size() {
                return Machine.closureSize;
            }

        }

        record BoolType() implements PrimType {

            @Override public int size() {
                return Machine.booleanSize;
            }

        }

        record IntType() implements PrimType {

            @Override public int size() {
                return Machine.integerSize;
            }

        }

        record CharType() implements PrimType {

            @Override public int size() {
                return Machine.characterSize;
            }

        }

        record VoidType() implements PrimType {

            @Override public int size() {
                return 0;
            }

        }

    }

    record ArrayType(int arraySize, RuntimeType elementType) implements RuntimeType {

        @Override public int size() {
            return elementType.size() * arraySize;
        }

    }

    record RecordType(List<FieldType> fieldTypes) implements RuntimeType {

        @Override public int size() {
            int totalSize = 0;

            for (FieldType fieldType : fieldTypes) {
                totalSize += fieldType.fieldType.size();
            }

            return totalSize;
        }

        public record FieldType(String fieldName, RuntimeType fieldType) { }

    }

    record RefOf(RuntimeType type) implements RuntimeType {

        public RefOf(RuntimeType type) {
            if (type instanceof RefOf) {
                throw new RuntimeException("creating ref of ref");
            }
            this.type = type;
        }

        @Override public int size() {
            return Machine.addressSize;
        }

        @Override public RuntimeType baseType() {
            return type;
        }

    }

}
