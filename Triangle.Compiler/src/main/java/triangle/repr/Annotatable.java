package triangle.repr;

// unfortunately java does not have `this`-typing so we have to cast here but this should always be safe
sealed public interface Annotatable {

    default <T extends Annotatable> T withAnnotationsOf(Annotatable that) {

        // unfortunately, Java's switch-case cannot match multiple cases, else we could have done something like:
        // switch(this) {
        //      case (SourceLocatable thisSourceLocatable) -> ...
        //      case (Typeable thisTypeable) -> ...
        // }
        // and have both cases run

        // but we cannot give up type-safety! so we do exhaustivity checking manually, with a maintained-by-hand list of
        // sub-interfaces of Annotatable

        // the code is formatted weirdly to allow easy copy-pasting when new Annotations are added
        //noinspection PointlessBooleanExpression
        if (
                !(this instanceof SourceLocatable) &&
                !(this instanceof Typeable) &&
                true
        ) {
            throw new RuntimeException("unknown annotation type");
        }

        if (this instanceof SourceLocatable thisSourceLocatable) {
            if (that instanceof SourceLocatable thatSourceLocatable) {
                thisSourceLocatable.setSourcePosition(thatSourceLocatable.sourcePosition());
            }
        }

        if (this instanceof Typeable thisTypeable) {
            if (that instanceof Typeable thatTypeable) {
                thisTypeable.setType(thatTypeable.getType());
            }
        }

        //noinspection unchecked
        return (T) this;
    }

    non-sealed interface SourceLocatable extends Annotatable {

        void setSourcePosition(SourcePosition sourcePos);

        SourcePosition sourcePosition();

        default <T extends SourceLocatable> T withSourcePosition(SourcePosition sourcePos) {
            this.setSourcePosition(sourcePos);
            //noinspection unchecked
            return (T) this;
        }
    }

    non-sealed interface Typeable extends Annotatable {

        Type getType();

        void setType(Type type);

        default <T extends Typeable> T withType(Type type) {
            this.setType(type);
            //noinspection unchecked
            return (T) this;
        }
    }

}
