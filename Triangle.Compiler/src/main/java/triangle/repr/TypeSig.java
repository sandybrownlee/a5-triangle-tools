package triangle.repr;

import triangle.util.ASTVisitor;

import java.util.List;

// represents "syntactic" types, i.e.,
sealed public interface TypeSig permits TypeSig.ArrayTypeSig, TypeSig.BasicTypeSig, TypeSig.RecordTypeSig, TypeSig.Void {

    public void visit(ASTVisitor visitor);

    record BasicTypeSig(String name) implements TypeSig {
        @Override public void visit(ASTVisitor visitor) {
            visitor.visitBasicTypeSig(this);
        }
    }

    record ArrayTypeSig(int arraySize, TypeSig elementTypeSig) implements TypeSig {
        @Override public void visit(ASTVisitor visitor) {
            visitor.visitArrayTypeSig(this);
        }
    }

    record RecordTypeSig(List<FieldType> fieldTypes) implements TypeSig {

        public record FieldType(String fieldName, TypeSig fieldTypeSig) { }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitRecordTypeSig(this);
        }
    }

    record Void() implements TypeSig {
        @Override public void visit(ASTVisitor visitor) {
            visitor.visitVoid(this);
        }
    }

}
