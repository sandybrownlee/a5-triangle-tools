package triangle.ast;

// convenience interface for visitor implementations
public interface AllVisitor<T, E extends Exception>
        extends Program.Visitor<T, E>, Argument.Visitor<T, E>, Declaration.Visitor<T, E>,
                Expression.Visitor<T, E>, Parameter.Visitor<T, E>, Statement.Visitor<T, E>, Type.Visitor<T, E>,
                Expression.Identifier.Visitor<T, E> { }
