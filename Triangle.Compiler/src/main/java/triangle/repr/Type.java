package triangle.repr;

import triangle.abstractMachine.Machine;

import java.util.List;

// interface that represents types that are "resolved", i.e.:
//      typeSig P ~ record x : Integer end
//      typeSig R ~ record x : P, y : P end
// is resolved into
//      typeSig P ~ record x : Integer end
//      typeSig R ~ record x : (record x : Integer), y : (record x : Integer) end
sealed public interface Type {

    // static instances of primitive types for convenience
    Type BOOL_TYPE = new PrimType.BoolType();
    Type INT_TYPE  = new PrimType.IntType();
    Type CHAR_TYPE = new PrimType.CharType();
    Type VOID_TYPE = new PrimType.VoidType();

    int size();

    // e.g: baseType(RefOf(RefOf(Integer)) = Integer
    default Type baseType() {
        return this;
    }

    default boolean isRef() { return this instanceof RefOf; }

    sealed interface PrimType extends Type
            permits PrimType.FuncType, PrimType.BoolType, PrimType.IntType, PrimType.CharType,
                    PrimType.VoidType {

        record FuncType(List<Type> argTypes, Type returnType) implements PrimType {

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

    record ArrayType(int arraySize, Type elementType) implements Type {

        @Override public int size() {
            return elementType.size() * arraySize;
        }

    }

    record RecordType(List<FieldType> fieldTypes) implements Type {

        @Override public int size() {
            int totalSize = 0;

            for (FieldType fieldType : fieldTypes) {
                totalSize += fieldType.fieldType.size();
            }

            return totalSize;
        }

        public record FieldType(String fieldName, Type fieldType) { }

    }

    record RefOf(Type type) implements Type {

        public RefOf {
            if (type instanceof RefOf) {
                // this can be handled, with some effort, but allowing it wont give our language any new features or better
                // performance, so I disallow it to keep the rest of the codebase simpler
                throw new RuntimeException("creating ref of ref");
            }
        }

        @Override public int size() {
            return Machine.addressSize;
        }

        @Override public Type baseType() {
            return type.baseType();
        }

    }

}
