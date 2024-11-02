package triangle.ast;

// convenience interface for visitor implementations
public interface AllVisitor<T> extends Program.Visitor<T>, Argument.Visitor<T>, Declaration.Visitor<T>, Expression.Visitor<T>,
                                       Parameter.Visitor<T>, Statement.Visitor<T>, Type.Visitor<T> { }
