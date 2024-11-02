package triangle.ast;

// convenience interface for visitor implementations
public interface AllVisitor<ST, T> extends Program.Visitor<ST, T>, Argument.Visitor<ST, T>, Declaration.Visitor<ST, T>,
                                           Expression.Visitor<ST, T>, Parameter.Visitor<ST, T>, Statement.Visitor<ST, T>,
                                           Type.Visitor<ST, T>, Expression.Identifier.Visitor<ST, T> { }
