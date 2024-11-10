package triangle.ast;

// visitor pattern is not actually needed because I am using sealed interfaces to emulate ADTs;
// this is a perfunctory implementation to complete Task 3
public interface ASTVisitor<T, E extends Exception> {

    // Argument
    T visit(Argument.VarArgument varArgument) throws E;

    T visit(Argument.FuncArgument funcArgument) throws E;

    // Declaration
    T visit(Declaration.ConstDeclaration constDeclaration) throws E;

    T visit(Declaration.VarDeclaration varDeclaration) throws E;

    T visit(Declaration.FuncDeclaration funcDeclaration) throws E;

    T visit(Declaration.TypeDeclaration typeDeclaration) throws E;

    // Expression
    T visit(Expression.LitBool litBool) throws E;

    T visit(Expression.LitInt litInt) throws E;

    T visit(Expression.LitChar litChar) throws E;

    T visit(Expression.LitArray litArray) throws E;

    T visit(Expression.LitRecord litRecord) throws E;

    T visit(Expression.UnaryOp unaryOp) throws E;

    T visit(Expression.BinaryOp binaryOp) throws E;

    T visit(Expression.LetExpression letExpression) throws E;

    T visit(Expression.IfExpression ifExpression) throws E;

    T visit(Expression.FunCall funCall) throws E;

    // Expression.Identifier
    T visit(Expression.Identifier.BasicIdentifier basicIdentifier) throws E;

    T visit(Expression.Identifier.RecordAccess recordAccess) throws E;

    T visit(Expression.Identifier.ArraySubscript arraySubscript) throws E;

    // Parameter
    T visit(Parameter.ValueParameter valueParameter) throws E;

    T visit(Parameter.VarParameter varParameter) throws E;

    T visit(Parameter.FuncParameter funcParameter) throws E;

    // Statement
    T visit(Statement.StatementBlock statementBlock) throws E;

    T visit(Statement.LetStatement letStatement) throws E;

    T visit(Statement.IfStatement ifStatement) throws E;

    T visit(Statement.WhileStatement whileStatement) throws E;

    T visit(Statement.LoopWhileStatement loopWhileStatement) throws E;

    T visit(Statement.RepeatWhileStatement repeatWhileStatement) throws E;

    T visit(Statement.RepeatUntilStatement repeatUntilStatement) throws E;

    T visit(Statement.AssignStatement assignStatement) throws E;

    // Type
    T visit(Type.BasicType basicType) throws E;

    T visit(Type.ArrayType arrayType) throws E;

    T visit(Type.RecordType recordType) throws E;

}
