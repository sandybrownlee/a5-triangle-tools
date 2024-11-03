package triangle.ast;

// convenience interface for visitor implementations
public interface AllVisitor<ST, T, E extends Exception>
        extends Program.Visitor<ST, T, E>, Argument.Visitor<ST, T, E>, Declaration.Visitor<ST, T, E>,
                Expression.Visitor<ST, T, E>, Parameter.Visitor<ST, T, E>, Statement.Visitor<ST, T, E>, Type.Visitor<ST, T, E>,
                Expression.Identifier.Visitor<ST, T, E> { }
